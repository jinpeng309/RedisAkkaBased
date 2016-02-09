package com.capslock.redis.client

import akka.actor._
import akka.stream.actor.ActorPublisher
import com.capslock.redis.command.info.InfoCommand.{INFO, INFO_RESP}
import com.capslock.redis.command.response.{NOT_NULL_BULK_STRING, BULK_ARRAY_RESP}
import com.capslock.redis.command.{RequestCommand, RespCommand}

/**
  * Created by capsl.
  */
class ClientSession(commandRouter: ActorRef, cacheManager: ActorRef) extends Actor with ActorLogging with ActorPublisher[RespCommand] {
  override def receive: Receive = {
    case INFO => onNext(INFO_RESP(BULK_ARRAY_RESP(List(NOT_NULL_BULK_STRING("redis_version:999.999.999")))))
    case requestCommand: RequestCommand =>
      cacheManager ! requestCommand
      sender() ! requestCommand
    case respCommand: RespCommand =>
      onNext(respCommand)
  }
}

object ClientSession {
  def props(commandRouter: ActorRef, cacheManager: ActorRef) = Props(classOf[ClientSession], commandRouter, cacheManager)
}
