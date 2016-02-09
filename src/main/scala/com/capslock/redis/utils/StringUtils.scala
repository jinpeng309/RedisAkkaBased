package com.capslock.redis.utils

/**
  * Created by capsl on 2016/2/9.
  */
object StringUtils {
  def safeStringToInt(str: String): Option[Int] = try {
    Some(str.toInt)
  } catch {
    case _: Throwable => None
  }
}
