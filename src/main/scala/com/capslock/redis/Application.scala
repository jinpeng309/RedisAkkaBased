package com.capslock.redis

import com.capslock.redis.server.Server

/**
  * Created by capsl.
  */
object Application extends App {
    Server.startServer("127.0.0.1", 6666)
}
