package com.capslock.redis.record

import akka.actor.{Actor, ActorLogging}
import com.capslock.redis.command.OK_RESP_COMMAND
import com.capslock.redis.command.response._
import com.capslock.redis.command.string.StringCommand._

/**
  * Created by capsl on 2016/2/8.
  */
class StringRecordManager extends Actor with ActorLogging {
  var stringValues = collection.mutable.Map[String, String]()

  override def receive: Receive = {
    case SET(key, value) =>
      stringValues.update(key, value)
      sender() ! OK_RESP_COMMAND
    case GET(key) =>
      sender() ! GET_RESP(stringValues.get(key).map(value => NOT_NULL_BULK_STRING(value)).getOrElse(NULL_BULK_STRING))
    case MGET(keyList) =>
      val resultList =  keyList.map(key => stringValues.get(key).map(value => NOT_NULL_BULK_STRING(value)).getOrElse(NULL_BULK_STRING))
      sender() ! MGET_RESP(BULK_ARRAY_RESP(resultList))
  }
}
