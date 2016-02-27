package com.capslock.redis.cache.record

import akka.actor.{Actor, ActorLogging}
import com.capslock.redis.command.list.ListCommand._
import com.capslock.redis.command.response.ERROR_RESP
import com.capslock.redis.command.response.RespCommand.{BULK_ARRAY_RESP_COMMAND, BULK_STRING_RESP_COMMAND, INTEGER_RESP_COMMAND}
import com.capslock.redis.command.{ERROR_RESP_COMMAND, OK_RESP_COMMAND}
import com.capslock.redis.utils.StringUtils

import scala.collection.mutable.ListBuffer

/**
  * Created by capsl on 2016/2/10.
  */
class ListRecord extends Actor with ActorLogging {
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
      listBuffer.appendAll(values)
      sender() ! INTEGER_RESP_COMMAND(listBuffer.size)

    case LPOP(_) =>
      sender() ! BULK_STRING_RESP_COMMAND(listBuffer.headOption)

    case LLEN(_) =>
      sender() ! INTEGER_RESP_COMMAND(listBuffer.size)

    case LINDEX(_, index) =>
      try {
        sender() ! BULK_STRING_RESP_COMMAND(listBuffer(index.toInt))
      } catch {
        case _: IndexOutOfBoundsException =>
          sender() ! ERROR_RESP_COMMAND(ERROR_RESP("error : out of index"))
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
          println(listBuffer.mkString(" "))
          sender() ! INTEGER_RESP_COMMAND(listBuffer.size)
        case Some(c) if c < 0 =>
          listBuffer = removeElementByCountForward(listBuffer.reverse, -c, value).reverse
          println(listBuffer.mkString(" "))
          sender() ! INTEGER_RESP_COMMAND(listBuffer.size)
        case Some(removeCount) if removeCount == 0 =>
          listBuffer = listBuffer.filter(data => !data.equals(value))
          println(listBuffer.mkString(" "))
          sender() ! INTEGER_RESP_COMMAND(listBuffer.size)
      }

    case LRANGE(_, start, stop) =>
      try {
        sender() ! BULK_ARRAY_RESP_COMMAND(listBuffer.slice(start.toInt, stop.toInt + 1).toList)
      } catch {
        case _: Throwable => log.warning("error start or stop")
      }
  }
}
