package com.capslock.redis.command.response


/**
  * Created by capsl on 2016/2/8.
  */

abstract class RESP()

object RESP {
  def encode(resp: RESP): String = {
    resp match {
      case OK_RESP => "+OK\r\n"
      case ERROR_RESP(errorMessage) => s"-Error $errorMessage\r\n"
      case INTEGER_RESP(value) => s":$value\r\n"
      case BULK_STRING_RESP(value) => "$" + s"${value.length}\r\n$value\r\n"
      case BULK_ARRAY_RESP(valueList) => "*" + s"${valueList.size}\r\n" + valueList.map(value => encode(value)).mkString
      case _ => "\r\n"
    }
  }
}


case object OK_RESP extends RESP

case class ERROR_RESP(msg: String) extends RESP

case class INTEGER_RESP(value: Int) extends RESP

case class BULK_STRING_RESP(value: String) extends RESP

case class BULK_ARRAY_RESP(bulkList: List[RESP]) extends RESP


