package com.capslock.redis.record

import akka.actor.{ActorRef, Actor, ActorLogging}
import com.capslock.redis.command.{ERROR_RESP_COMMAND, OK_RESP_COMMAND}
import com.capslock.redis.command.response._
import com.capslock.redis.command.string.StringCommand._
import com.capslock.redis.utils.StringUtils

/**
  * Created by capsl on 2016/2/8.
  */
class StringRecordManager extends Actor with ActorLogging {
  var stringValues = collection.mutable.Map[String, String]()

  private def updateNumberWithStep(key: String, step: Int, sender: ActorRef): Unit = {
    StringUtils.safeStringToInt(stringValues.getOrElseUpdate(key, "0")) match {
      case Some(number) =>
        stringValues.update(key, (number + step).toString)
        sender ! INTEGER_RESP_COMMAND(INTEGER_RESP(number + step))
      case None =>
        sender ! ERROR_RESP_COMMAND(ERROR_RESP("not a number"))
    }
  }

  override def receive: Receive = {
    case SET(key, value) =>
      stringValues.update(key, value)
      sender() ! OK_RESP_COMMAND

    case GET(key) =>
      sender() ! BULK_STRING_RESP_COMMAND(stringValues.get(key).map(value => NOT_NULL_BULK_STRING(value)).getOrElse(NULL_BULK_STRING))

    case MGET(keyList) =>
      val resultList = keyList.map(key => stringValues.get(key).map(value => NOT_NULL_BULK_STRING(value)).getOrElse(NULL_BULK_STRING))
      sender() ! BULK_ARRAY_RESP_COMMAND(BULK_ARRAY_RESP(resultList))

    case MSET(keyValueList) =>
      keyValueList.sliding(2, 2).foreach { case List(key, value) => stringValues.update(key, value) }
      sender() ! OK_RESP_COMMAND

    case STRLEN(key) =>
      val length = stringValues.get(key).map(_.length).getOrElse(0)
      sender() ! INTEGER_RESP_COMMAND(INTEGER_RESP(length))

    case INCR(key) =>
      updateNumberWithStep(key, 1, sender())

    case INCRBY(key, step) =>
      updateNumberWithStep(key, step.toInt, sender())

    case DECR(key) =>
      updateNumberWithStep(key, -1, sender())

    case DECRBY(key, step) =>
      updateNumberWithStep(key, -step.toInt, sender())

    case GETRANGE(key, start, end) =>
      val startIndex = StringUtils.safeStringToInt(start)
      val endIndex = StringUtils.safeStringToInt(end)
      if (startIndex.isDefined && endIndex.isDefined) {
        val subString = stringValues.get(key).map(value => NOT_NULL_BULK_STRING(StringUtils.subString(value, start.toInt, end.toInt )))
          .getOrElse(NULL_BULK_STRING)
        sender() ! BULK_STRING_RESP_COMMAND(subString)
      }
  }
}
