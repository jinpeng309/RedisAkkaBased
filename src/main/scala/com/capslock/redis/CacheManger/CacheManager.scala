package com.capslock.redis.CacheManger

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.capslock.redis.command.NoneRequestCommand
import com.capslock.redis.command.string.StringCommand.{GET, SET}
import com.capslock.redis.record.StringRecord

/**
  * Created by capsl on 2016/2/8.
  */
class CacheManager extends Actor with ActorLogging {
  var records = collection.mutable.Map[String, ActorRef]()

  override def receive: Receive = {
    case NoneRequestCommand => log.info("ignore NoneCommand")
    case cmd: SET =>
      records.getOrElseUpdate(cmd.key, context.actorOf(Props[StringRecord])).forward(cmd)
    case cmd: GET =>
      records.get(cmd.key) match {
        case Some(record) => record.forward(cmd)
        case _ => sender() ! NoneRequestCommand
      }
  }
}
