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

  def safeStringToFloat(str: String): Option[Float] = try {
    Some(str.toFloat)
  } catch {
    case _: Throwable => None
  }

  def subString(str: String, startIndex: Int, endIndex: Int): String = {
    val range = Range(-str.length, str.length)
    val start = IndexUtils.roundIndexInRange(startIndex, str.length, range)
    val end = IndexUtils.roundIndexInRange(endIndex, str.length, range) + 1
    str.substring(start, end)
  }
}
