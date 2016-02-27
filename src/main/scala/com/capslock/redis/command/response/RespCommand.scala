package com.capslock.redis.command.response

import com.capslock.redis.command.RespCommand

/**
  * Created by capsl.
  */
object RespCommand {

  case class BULK_STRING_RESP_COMMAND(resp: BULK_STRING_RESP) extends RespCommand

  case class BULK_ARRAY_RESP_COMMAND(resp: BULK_ARRAY_RESP) extends RespCommand

  case class INTEGER_RESP_COMMAND(resp: INTEGER_RESP) extends RespCommand

  object INTEGER_RESP_COMMAND {
    def apply(value: Int): INTEGER_RESP_COMMAND = {
      INTEGER_RESP_COMMAND(INTEGER_RESP(value))
    }
  }

  object BULK_STRING_RESP_COMMAND {
    def apply(value: Option[String]): BULK_STRING_RESP_COMMAND = {
      value match {
        case Some(data) =>
          BULK_STRING_RESP_COMMAND(NOT_NULL_BULK_STRING(data))
        case None =>
          BULK_STRING_RESP_COMMAND(NULL_BULK_STRING)
      }
    }

    def apply(value: String): BULK_STRING_RESP_COMMAND = {
      BULK_STRING_RESP_COMMAND(NOT_NULL_BULK_STRING(value))
    }
  }

  object BULK_ARRAY_RESP_COMMAND {
    def apply(values: List[String]): BULK_ARRAY_RESP_COMMAND = {
      BULK_ARRAY_RESP_COMMAND(BULK_ARRAY_RESP(values.map(value => NOT_NULL_BULK_STRING(value))))
    }
  }

}

