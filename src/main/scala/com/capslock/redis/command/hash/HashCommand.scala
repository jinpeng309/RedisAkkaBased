package com.capslock.redis.command.hash

import com.capslock.redis.command.RequestCommand

/**
  * Created by capsl on 2016/2/10.
  */
abstract class HashCommand extends RequestCommand {
  def key: String
}

object HashCommand {

  case class HSET(key: String, field: String, value: String) extends HashCommand

  case class HGET(key: String, field: String) extends HashCommand

  case class HDEL(key: String, fields: List[String]) extends HashCommand

  case class HEXISTS(key: String, field: String) extends HashCommand

  case class HGETALL(key: String) extends HashCommand

  case class HINCRBY(key: String, field: String, step: String) extends HashCommand

  case class HINCRBYFLOAT(key: String, field: String, step: String) extends HashCommand

  case class HKEYS(key: String) extends HashCommand

  case class HLEN(key: String) extends HashCommand

  case class HMGET(key: String, fields: List[String]) extends HashCommand

  case class HMSET(key: String, fields: List[String]) extends HashCommand

  case class HSETNX(key: String, field: String, value: String) extends HashCommand

  case class HSTRLEN(key: String, field: String) extends HashCommand

  case class HVALS(key: String) extends HashCommand

}

