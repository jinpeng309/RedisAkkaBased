package com.capslock.redis.command

import com.capslock.redis.command.hash.HashCommand._
import com.capslock.redis.command.list.ListCommand._
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
      //Strings
      case List("SET", key, value) => SET(key, value)
      case List("GET", key) => GET(key)
      case "MGET" :: keyList => MGET(keyList)
      case "MSET" :: keyValueList => MSET(keyValueList)
      case List("STRLEN", key) => STRLEN(key)
      case List("INCR", key) => INCR(key)
      case List("INCRBY", key, step) => INCRBY(key, step)
      case List("DECR", key) => DECR(key)
      case List("DECRBY", key, step) => DECRBY(key, step)
      case List("GETRANGE", key, start, end) => GETRANGE(key, start, end)
      case List("SETEX", key, expireTime, value) => SETEX(key, expireTime, value)

      //Hashes
      case List("HSET", key, field, value) => HSET(key, field, value)
      case List("HGET", key, field) => HGET(key, field)
      case "HDEL" :: key :: fields => HDEL(key, fields)
      case List("HEXISTS", key, field) => HEXISTS(key, field)
      case List("HGETALL", key) => HGETALL(key)
      case List("HINCRBY", key, field, step) => HINCRBY(key, field, step)
      case List("HINCRBYFLOAT", key, field, step) => HINCRBYFLOAT(key, field, step)
      case List("HKEYS", key) => HKEYS(key)
      case List("HLEN", key) => HLEN(key)
      case "HMGET" :: key :: fields => HMGET(key, fields)
      case "HMSET" :: key :: fieldValueList => HMSET(key, fieldValueList)
      case List("HSETNX", key, field, value) => HSETNX(key, field, value)
      case List("HSTRLEN", key, field) => HSTRLEN(key, field)
      case List("HVALS", key) => HVALS(key)

      //Lists
      case "LPUSH" :: key :: values => LPUSH(key, values)
      case List("LPOP", key) => LPOP(key)
      case List("LLEN", key) => LLEN(key)
      case List("LINDEX", key, value) => LINDEX(key, value)
      case List("LSET", key, index, value) => LSET(key, index, value)
      case List("LREM", key, count, value) => LREM(key, count, value)
      case List("LRANGE", key, start, stop) => LRANGE(key, start, stop)
      case List("BLPOP", key, timeout) => BLPOP(key, timeout)

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



