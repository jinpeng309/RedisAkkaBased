package com.capslock.redis.cache.record

import java.util

import akka.actor.{Actor, ActorLogging, Stash}
import com.capslock.redis.command.list.ListCommand._
import com.capslock.redis.command.response.ERROR_RESP
import com.capslock.redis.command.response.RespCommand.{BULK_ARRAY_RESP_COMMAND, BULK_STRING_RESP_COMMAND, INTEGER_RESP_COMMAND}
import com.capslock.redis.command.{ERROR_RESP_COMMAND, OK_RESP_COMMAND}
import com.capslock.redis.utils.StringUtils

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Created by capsl on 2016/2/10.
  */
class ListRecord extends Actor with ActorLogging with Stash {
  var listBuffer = ListBuffer.empty[String]

  private def removeElementByCountForward(list: ListBuffer[String], count: Int, value: String) = {
    var removeCount = count
    val newListBuffer = ListBuffer.empty[String]
    for (elem <- list) {
      if (removeCount > 0) {
        if (elem.equals(value)) {
          removeCount -= 1
        } else {
          newListBuffer.append(elem)
        }
      } else {
        newListBuffer.append(elem)
      }
    }
    newListBuffer
  }

  override def receive: Receive = {
    case LPUSH(_, values) =>
      listBuffer.insertAll(0, values.reverse)
      sender() ! INTEGER_RESP_COMMAND(listBuffer.size)

    case LPOP(_) =>
      listBuffer.headOption match {
        case Some(value) =>
          listBuffer.remove(0)
          sender() ! BULK_STRING_RESP_COMMAND(value)
        case _ => sender() ! BULK_STRING_RESP_COMMAND(None)
      }

    case LLEN(_) =>
      sender() ! INTEGER_RESP_COMMAND(listBuffer.size)

    case LINDEX(_, index) =>
      try {
        sender() ! BULK_STRING_RESP_COMMAND(listBuffer(index.toInt))
      } catch {
        case _: IndexOutOfBoundsException =>
          sender() ! BULK_STRING_RESP_COMMAND(None)
        case _: NumberFormatException =>
          sender() ! ERROR_RESP_COMMAND(ERROR_RESP("error: index not int value"))
      }

    case LSET(_, index, value) =>
      try {
        listBuffer.update(index.toInt, value)
        sender() ! OK_RESP_COMMAND
      } catch {
        case _: Throwable => sender() ! ERROR_RESP_COMMAND(ERROR_RESP("error"))
      }

    case LREM(_, count, value) =>
      StringUtils.safeStringToInt(count) match {
        case Some(c) if c > 0 =>
          listBuffer = removeElementByCountForward(listBuffer, c, value)
          sender() ! INTEGER_RESP_COMMAND(listBuffer.size)
        case Some(c) if c < 0 =>
          listBuffer = removeElementByCountForward(listBuffer.reverse, -c, value).reverse
          sender() ! INTEGER_RESP_COMMAND(listBuffer.size)
        case Some(removeCount) if removeCount == 0 =>
          listBuffer = listBuffer.filter(data => !data.equals(value))
          sender() ! INTEGER_RESP_COMMAND(listBuffer.size)
      }

    case LRANGE(_, start, stop) =>
      try {
        sender() ! BULK_ARRAY_RESP_COMMAND(listBuffer.slice(start.toInt, stop.toInt + 1).toList)
      } catch {
        case _: Throwable => log.warning("error start or stop")
      }

    case BLPOP(key, timeout) =>
      listBuffer.headOption match {
        case Some(value) =>
          listBuffer = listBuffer.tail
          sender() ! BULK_ARRAY_RESP_COMMAND(List(key, value))
        case None =>
          val client = sender()
          var clientList = ListBuffer(client)
          context.become({
            case BLPOP(_, t) =>
              val blockClient = sender()
              clientList.append(blockClient)
            case LPUSH(_, values) =>
              listBuffer.appendAll(values)
              val listHead = listBuffer.head
              listBuffer = listBuffer.dropRight(1)
              clientList.head ! BULK_ARRAY_RESP_COMMAND(List(key, listHead))
              clientList = clientList.tail
              if (clientList.isEmpty) {
                context.unbecome()
              }
          })
      }
  }
}
