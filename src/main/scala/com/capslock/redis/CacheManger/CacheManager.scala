package com.capslock.redis.CacheManger

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.capslock.redis.command.NoneRequestCommand
import com.capslock.redis.command.string.StringCommand
import com.capslock.redis.record.StringRecordManager

/**
  * Created by capsl on 2016/2/8.
  */
class CacheManager extends Actor with ActorLogging {
  val stringRecordManager: ActorRef = context.actorOf(Props[StringRecordManager])

  override def receive: Receive = {
    case NoneRequestCommand => log.info("ignore NoneCommand")
    case cmd: StringCommand => stringRecordManager.forward(cmd)
  }
}
