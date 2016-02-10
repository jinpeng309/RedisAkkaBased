package com.capslock.redis.record

import akka.actor.{Actor, ActorLogging}
import com.capslock.redis.command.hash.HashCommand.HSET
import com.capslock.redis.command.string.StringCommand.INTEGER_RESP_COMMAND

/**
  * Created by capsl on 2016/2/10.
  */
class HashRecord extends Actor with ActorLogging {
  var map = collection.mutable.Map[String, String]()

  implicit def bool2int(b: Boolean): Int = if (b) 1 else 0

  override def receive: Receive = {
    case HSET(_, key, value) =>
      val containsKey = map.contains(key)
      map.update(key, value)
      sender() ! INTEGER_RESP_COMMAND(containsKey)
  }
}
