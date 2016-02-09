package com.capslock.redis.cache

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.capslock.redis.command.response.{INTEGER_RESP, NULL_BULK_STRING}
import com.capslock.redis.command.string.StringCommand
import com.capslock.redis.command.string.StringCommand._
import com.capslock.redis.command.{NoneRequestCommand, RequestCommand, RespCommand}
import com.capslock.redis.record.StringRecord

import scala.reflect.ClassTag

/**
  * Created by capsl on 2016/2/8.
  */
class CacheManager extends Actor with ActorLogging {
  var records = collection.mutable.Map[String, ActorRef]()

  private def forwardCmdOrReply(key: String, command: RequestCommand, reply: RespCommand): Unit = {
    if (records.contains(key)) {
      records.get(key).get.forward(command)
    } else {
      sender() ! reply
    }
  }

  private def getOrCreateRecordForForward[RecordType <: Actor:ClassTag](key: String, cmd: RequestCommand): Unit = {
    records.getOrElseUpdate(key, context.actorOf(Props[RecordType])).forward(cmd)
  }


  private def processStringCommand(command: StringCommand): Unit = {
    command match {
      case MSET(keyList: List[String]) =>

      case MGET(keyValueList: List[String]) =>

      case SET(key, _) => getOrCreateRecordForForward[StringRecord](key, command)

      case GET(key) => forwardCmdOrReply(key, command, BULK_STRING_RESP_COMMAND(NULL_BULK_STRING))

      case STRLEN(key) => forwardCmdOrReply(key, command, INTEGER_RESP_COMMAND(INTEGER_RESP(0)))

      case INCR(key) => getOrCreateRecordForForward[StringRecord](key, command)

      case INCRBY(key, _) => getOrCreateRecordForForward[StringRecord](key, command)

      case DECR(key) => getOrCreateRecordForForward[StringRecord](key, command)

      case DECRBY(key, _) => getOrCreateRecordForForward[StringRecord](key, command)

      case GETRANGE(key, _, _) => getOrCreateRecordForForward[StringRecord](key, command)
    }
  }


  override def receive: Receive = {
    case NoneRequestCommand => log.info("ignore NoneCommand")

    case command: StringCommand => processStringCommand(command)
  }
}
