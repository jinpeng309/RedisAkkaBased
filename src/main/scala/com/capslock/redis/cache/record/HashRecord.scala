package com.capslock.redis.cache.record

import akka.actor.{Actor, ActorLogging}
import com.capslock.redis.command.hash.HashCommand._
import com.capslock.redis.command.response.RespCommand.{BULK_ARRAY_RESP_COMMAND, BULK_STRING_RESP_COMMAND, INTEGER_RESP_COMMAND}
import com.capslock.redis.command.response.{BULK_ARRAY_RESP, ERROR_RESP, NOT_NULL_BULK_STRING, NULL_BULK_STRING}
import com.capslock.redis.command.{ERROR_RESP_COMMAND, OK_RESP_COMMAND}
import com.capslock.redis.utils.StringUtils

/**
  * Created by capsl on 2016/2/10.
  */
class HashRecord extends Actor with ActorLogging {
  var map = collection.mutable.Map[String, String]()

  implicit def bool2int(b: Boolean): Int = if (b) 1 else 0

  private def increaseNumberWithIntStep(key: String, step: Int): Either[String, Int] = {
    val oldValue = map.getOrElse(key, "0")
    StringUtils.safeStringToInt(oldValue) match {
      case Some(number) =>
        val newValue = (number + step).toString
        map.update(key, newValue)
        Right(number + step)
      case _ => Left("not a number")
    }
  }

  private def increaseNumberWithFloatStep(key: String, step: Float): Either[String, Float] = {
    val oldValue = map.getOrElse(key, "0")
    StringUtils.safeStringToInt(oldValue) match {
      case Some(number) =>
        val newValue = (number + step).toString
        map.update(key, newValue)
        Right(number + step)
      case _ => Left("not a number")
    }
  }

  override def receive: Receive = {
    case HSET(_, key, value) =>
      val containsKey = map.contains(key)
      map.update(key, value)
      sender() ! INTEGER_RESP_COMMAND(!containsKey)

    case HGET(_, key) =>
      sender() ! BULK_STRING_RESP_COMMAND(map.get(key))

    case HDEL(_, fields) =>
      var removedKeys = 0
      fields.foreach({ field =>
        map.remove(field) match {
          case Some(_) => removedKeys += 1
        }
      })
      sender() ! INTEGER_RESP_COMMAND(removedKeys)

    case HEXISTS(_, key) =>
      sender() ! INTEGER_RESP_COMMAND(map.contains(key))

    case HGETALL(_) =>
      val kvList = map.foldLeft[List[String]](List[String]()) {
        case (list, (key, value)) => key :: value :: list
      }
      sender() ! BULK_ARRAY_RESP_COMMAND(kvList)

    case HINCRBY(_, key, step) =>
      increaseNumberWithIntStep(key, step.toInt) match {
        case Left(errorMsg) => sender() ! ERROR_RESP_COMMAND(ERROR_RESP(errorMsg))
        case Right(newValue) => sender() ! INTEGER_RESP_COMMAND(newValue)
      }

    case HINCRBYFLOAT(_, key, step) =>
      increaseNumberWithFloatStep(key, step.toFloat) match {
        case Left(errorMsg) => sender() ! ERROR_RESP_COMMAND(ERROR_RESP(errorMsg))
        case Right(newValue) => sender() ! BULK_STRING_RESP_COMMAND(newValue.toString)
      }

    case HKEYS(_) =>
      sender() ! BULK_ARRAY_RESP_COMMAND(map.keys.toList)

    case HLEN(_) =>
      sender() ! INTEGER_RESP_COMMAND(map.size)

    case HMGET(_, keys) =>
      val valueResp = keys.map(field => map.get(field).map(value => NOT_NULL_BULK_STRING(value)).getOrElse(NULL_BULK_STRING))
      sender() ! BULK_ARRAY_RESP_COMMAND(BULK_ARRAY_RESP(valueResp))

    case HMSET(_, keyValueList) =>
      keyValueList.sliding(2, 2).foreach {
        case List(key, value) => map.update(key, value)
      }
      sender() ! OK_RESP_COMMAND

    case HSETNX(_, key, value) =>
      val containsKey = map.contains(key)
      if (!containsKey) {
        map.update(key, value)
      }
      sender() ! INTEGER_RESP_COMMAND(!containsKey)

    case HSTRLEN(_, key) =>
      val valueLength = map.get(key).map(value => value.length).getOrElse(0)
      sender() ! INTEGER_RESP_COMMAND(valueLength)

    case HVALS(_) =>
      sender() ! BULK_ARRAY_RESP_COMMAND(map.values.toList)
  }
}
