package com.capslock.redis.command

import com.capslock.redis.command.response.{OK_RESP, RESP}
import com.capslock.redis.command.string.StringCommand._

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
      case "MGET" :: keyList => MGET(keyList)
      case "MSET" :: keyValueList => MSET(keyValueList)
      case List("STRLEN", key) => STRLEN(key)
      case List("INCR", key) => INCR(key)
      case List("INCRBY", key, step) => INCRBY(key, step)
      case List("DECR", key) => DECR(key)
      case List("DECRBY", key, step) => DECRBY(key, step)
    }
  }
}


abstract class RespCommand extends Command {
  def resp: RESP
}

case object OK_RESP_COMMAND extends RespCommand {
  override def resp: RESP = OK_RESP
}

case class ERROR_RESP_COMMAND(resp: RESP) extends RespCommand



