package com.capslock.redis.record

import akka.actor.{Actor, ActorLogging}
import com.capslock.redis.command.response.BULK_STRING_RESP
import com.capslock.redis.command.string.StringCommand.{GET_RESP, GET, SET}

/**
  * Created by capsl on 2016/2/8.
  */
class StringRecord extends Actor with ActorLogging {
  var value: String = ""

  override def receive: Receive = {
    case SET(_, newValue) =>
      value = newValue
    case GET(_) =>
      sender() ! GET_RESP(BULK_STRING_RESP(value))
  }
}
