package com.capslock.redis.command.hash

import com.capslock.redis.command.RequestCommand

/**
  * Created by capsl on 2016/2/10.
  */
class HashCommand extends RequestCommand

object HashCommand {

  case class HSET(key: String, field: String, value: String) extends HashCommand

}

