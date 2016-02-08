package com.capslock.redis.command

import com.capslock.redis.command.response.RESP
import com.capslock.redis.command.string.StringCommand.{GET, SET}

/**
  * Created by capsl.
  */

case object NoneRequestCommand extends RequestCommand {
  override def key: String = ""
}

abstract class Command

abstract class RequestCommand extends Command {
  def key: String
}

object RequestCommand {
  def apply(argumentList: List[String]): Command = {
    argumentList match {
      case List("SET", key, value) => SET(key, value)
      case List("GET", key) => GET(key)
    }
  }
}


abstract class RespCommand extends Command {
  def resp: RESP
}


