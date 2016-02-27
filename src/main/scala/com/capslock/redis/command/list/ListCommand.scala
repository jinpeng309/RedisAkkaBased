package com.capslock.redis.command.list

import com.capslock.redis.command.RequestCommand

/**
  * Created by capsl.
  */
abstract class ListCommand extends RequestCommand {
  def key: String
}

object ListCommand {

  case class LPUSH(key: String, values: List[String]) extends ListCommand

  case class LPOP(key: String) extends ListCommand

  case class LLEN(key: String) extends ListCommand

  case class LINDEX(key: String, index: String) extends ListCommand

  case class LSET(key: String, index: String, value: String) extends ListCommand

  case class LREM(key: String, count: String, value: String) extends ListCommand

  case class LRANGE(key: String, start: String, stop: String) extends ListCommand

  case class BLPOP(key: String, timeout: String) extends ListCommand

}
