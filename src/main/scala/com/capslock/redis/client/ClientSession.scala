package com.capslock.redis.client

import akka.actor._
import akka.stream.actor.ActorPublisher
import com.capslock.redis.command.{RequestCommand, Command, RespCommand}

/**
  * Created by capsl.
  */
class ClientSession(commandRouter: ActorRef, cacheManager: ActorRef) extends Actor with ActorLogging with ActorPublisher[RespCommand] {
  override def receive: Receive = {
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
