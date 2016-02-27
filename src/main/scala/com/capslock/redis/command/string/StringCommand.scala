package com.capslock.redis.command.string

import com.capslock.redis.command.response._
import com.capslock.redis.command.{RespCommand, RequestCommand}

/**
  * Created by capsl on 2016/2/8.
  */
class StringCommand extends RequestCommand

object StringCommand {

  case class SET(key: String, value: String) extends StringCommand

  case class SET_AND_SUSPEND(value: String) extends StringCommand

  case object RESUME extends StringCommand

  case object ROLL_BACK extends StringCommand

  case class GET(key: String) extends StringCommand

  case class QUERY(key: String) extends StringCommand

  case class MGET(keyList: List[String]) extends StringCommand

  case class MSET(keyValueList: List[String]) extends StringCommand

  case class STRLEN(key: String) extends StringCommand

  case class INCR(key: String) extends StringCommand

  case class INCRBY(key: String, step: String) extends StringCommand

  case class DECR(key: String) extends StringCommand

  case class DECRBY(key: String, step: String) extends StringCommand

  case class GETRANGE(key: String, start: String, end: String) extends StringCommand

  case class SETEX(key: String, expireTime: String, value: String) extends StringCommand


}
