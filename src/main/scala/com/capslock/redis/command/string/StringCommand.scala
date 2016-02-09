package com.capslock.redis.command.string

import com.capslock.redis.command.response.{BULK_ARRAY_RESP, BULK_STRING_RESP}
import com.capslock.redis.command.{RespCommand, RequestCommand}

/**
  * Created by capsl on 2016/2/8.
  */
class StringCommand extends RequestCommand

object StringCommand {

  case class SET(key: String, value: String) extends StringCommand

  case class GET(key: String) extends StringCommand

  case class MGET(keyList: List[String]) extends StringCommand

  case class GET_RESP(resp: BULK_STRING_RESP) extends RespCommand

  case class MGET_RESP(resp: BULK_ARRAY_RESP) extends RespCommand
}
