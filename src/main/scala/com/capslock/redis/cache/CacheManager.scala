package com.capslock.redis.cache

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.capslock.redis.command.response._
import com.capslock.redis.command.string.StringCommand
import com.capslock.redis.command.string.StringCommand._
import com.capslock.redis.command.{OK_RESP_COMMAND, NoneRequestCommand, RequestCommand, RespCommand}
import com.capslock.redis.record.StringRecord

import scala.concurrent.Future
import scala.concurrent.duration._
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

  private def getOrCreateRecordForForward[RecordType <: Actor : ClassTag](key: String, cmd: RequestCommand): Unit = {
    records.getOrElseUpdate(key, context.actorOf(Props[RecordType])).forward(cmd)
  }

  private def getOrCreateRecord[RecordType <: Actor : ClassTag](key: String): ActorRef = {
    records.getOrElseUpdate(key, context.actorOf(Props[RecordType]))
  }

  private def processStringCommand(command: StringCommand): Unit = {
    implicit val timeout = Timeout(1 seconds)
    implicit val dispatcher = context.system.dispatcher

    command match {
      case MSET(keyValueList: List[String]) =>
        val clientSender = sender()
        val futureList = Future.sequence(keyValueList.sliding(2, 2).map({
          case List(key, value) => getOrCreateRecord[StringRecord](key).ask(SET_AND_SUSPEND(value))
        }))

        futureList.failed.foreach {
          case _ => keyValueList.sliding(2, 2).foreach({
            case List(key, value) => getOrCreateRecord(key) ! ROLL_BACK
          })
        }
        futureList.foreach(resultList => {
          if (resultList.exists(result => result != OK_RESP_COMMAND)) {
            keyValueList.sliding(2, 2).foreach({
              case List(key, value) => getOrCreateRecord(key) ! ROLL_BACK
            })
          } else {
            keyValueList.sliding(2, 2).foreach({
              case List(key, value) => getOrCreateRecord(key) ! RESUME
            })
          }
          clientSender ! OK_RESP_COMMAND
        })

      case MGET(keyList: List[String]) =>
        val clientSender = sender()
        val futureList = Future.sequence(keyList.map(key => {
          if (records.contains(key)) {
            records.get(key).get.ask(QUERY(key))
          } else {
            Future.successful(NULL_BULK_STRING)
          }
        }))
        futureList.map(resultList =>
          clientSender ! BULK_ARRAY_RESP_COMMAND(BULK_ARRAY_RESP(resultList.asInstanceOf[List[BULK_STRING_RESP]])))

      case SET(key, _) => getOrCreateRecordForForward[StringRecord](key, command)

      case GET(key) => forwardCmdOrReply(key, command, BULK_STRING_RESP_COMMAND(NULL_BULK_STRING))

      case STRLEN(key) => forwardCmdOrReply(key, command, INTEGER_RESP_COMMAND(0))

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
