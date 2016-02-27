package com.capslock.redis.cache.record

import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor._
import com.capslock.redis.command.list.ListCommand._
import com.capslock.redis.command.response.ERROR_RESP
import com.capslock.redis.command.response.RespCommand.{BULK_ARRAY_RESP_COMMAND, BULK_STRING_RESP_COMMAND, INTEGER_RESP_COMMAND}
import com.capslock.redis.command.{ERROR_RESP_COMMAND, OK_RESP_COMMAND}
import com.capslock.redis.utils.StringUtils

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.FiniteDuration

/**
  * Created by capsl on 2016/2/10.
  */
class ListRecord extends Actor with ActorLogging with Stash {
  var listBuffer = ListBuffer.empty[String]
  implicit val dispatcher = context.system.dispatcher

  case class BlockClient(clientId: String, client: ActorRef, timer: Cancellable)

  case class Timeout(clientId: String)

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
          val uuid = UUID.randomUUID().toString
          val recordActor: ActorRef = self
          val cancel = context.system.scheduler.scheduleOnce(FiniteDuration(timeout.toLong, TimeUnit.SECONDS)) {
            recordActor ! Timeout(uuid)
          }
          val client = BlockClient(uuid, sender(), cancel)
          val blockClientList = ListBuffer(client)
          context.become({
            case BLPOP(_, t) =>
              val uuid = UUID.randomUUID().toString
              val cancel = context.system.scheduler.scheduleOnce(FiniteDuration(t.toLong, TimeUnit.SECONDS)) {
                recordActor ! Timeout(uuid)
              }
              val blockClient = BlockClient(uuid, sender(), cancel)
              blockClientList.append(blockClient)

            case LPUSH(_, values) =>
              listBuffer.insertAll(0, values)
              for (element <- listBuffer; blockClient <- blockClientList) {
                listBuffer.remove(0)
                blockClientList.remove(0)
                blockClient.client ! BULK_ARRAY_RESP_COMMAND(List(key, element))
              }
              if (blockClientList.isEmpty) {
                context.unbecome()
              }
            case Timeout(id) =>
              blockClientList.find(blockClient => blockClient.clientId == id) match {
                case Some(blockClient) =>
                  blockClient.client ! BULK_STRING_RESP_COMMAND(None)
                  blockClient.timer.cancel()
                case _ =>
              }
          })
      }
    case _ =>
  }
}
