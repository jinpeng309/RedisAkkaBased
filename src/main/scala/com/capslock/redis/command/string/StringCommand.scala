package com.capslock.redis.command.string

import com.capslock.redis.command.response.{INTEGER_RESP, BULK_ARRAY_RESP, BULK_STRING_RESP}
import com.capslock.redis.command.{RespCommand, RequestCommand}

/**
  * Created by capsl on 2016/2/8.
  */
class StringCommand extends RequestCommand

object StringCommand {

  case class SET(key: String, value: String) extends StringCommand

  case class GET(key: String) extends StringCommand

  case class MGET(keyList: List[String]) extends StringCommand

  case class MSET(keyValueList: List[String]) extends StringCommand

  case class STRLEN(key: String) extends StringCommand

  case class INCR(key: String) extends StringCommand

  case class INCRBY(key: String, step: String) extends StringCommand

  case class DECR(key: String) extends StringCommand

  case class DECRBY(key: String, step: String) extends StringCommand

  case class GETRANGE(key: String, start: String, end: String) extends StringCommand

  case class SETEX(key: String, expireTime: String, value: String) extends StringCommand

  case class BULK_STRING_RESP_COMMAND(resp: BULK_STRING_RESP) extends RespCommand

  case class BULK_ARRAY_RESP_COMMAND(resp: BULK_ARRAY_RESP) extends RespCommand

  case class INTEGER_RESP_COMMAND(resp: INTEGER_RESP) extends RespCommand

}
