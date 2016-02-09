package com.capslock.redis.command.info

import com.capslock.redis.command.response.BULK_ARRAY_RESP
import com.capslock.redis.command.{RequestCommand, RespCommand}

/**
  * Created by capsl on 2016/2/9.
  */
object InfoCommand {
  case object INFO extends RequestCommand

  case class INFO_RESP(resp: BULK_ARRAY_RESP) extends RespCommand
}
