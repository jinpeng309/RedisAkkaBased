package com.capslock.redis.command.string

import com.capslock.redis.command.response.BULK_STRING_RESP
import com.capslock.redis.command.{RespCommand, RequestCommand}

/**
  * Created by capsl on 2016/2/8.
  */
object StringCommand {

  case class SET(key: String, value: String) extends RequestCommand

  case class GET(key: String) extends RequestCommand

  case class GET_RESP(resp: BULK_STRING_RESP) extends RespCommand
}
