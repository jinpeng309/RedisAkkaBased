package com.capslock.redis.command

import com.capslock.redis.command.info.InfoCommand.INFO
import com.capslock.redis.command.response.{OK_RESP, RESP}
import com.capslock.redis.command.string.StringCommand.{MGET, GET, SET}

/**
  * Created by capsl.
  */

case object NoneRequestCommand extends RequestCommand

abstract class Command

abstract class RequestCommand extends Command

object RequestCommand {
  def apply(argumentList: List[String]): Command = {
    argumentList match {
      case List("SET", key, value) => SET(key, value)
      case List("GET", key) => GET(key)
      case "MGET"::keyList => MGET(keyList)
      case List("INFO") => INFO
    }
  }
}


abstract class RespCommand extends Command {
  def resp: RESP
}

case object OK_RESP_COMMAND extends RespCommand {
  override def resp: RESP = OK_RESP
}



