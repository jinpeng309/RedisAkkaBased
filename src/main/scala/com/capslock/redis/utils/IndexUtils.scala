package com.capslock.redis.utils

/**
  * Created by capsl.
  */
object IndexUtils {
  def roundIndexInRange(index: Int, roundDelta: Int, range: Range): Int = {
    if (range.contains(index)) {
      if (index >= 0) {
        index
      } else {
        roundDelta + index
      }
    } else {
      if (index > 0) {
        roundDelta - 1
      } else {
        -roundDelta
      }
    }
  }
}
