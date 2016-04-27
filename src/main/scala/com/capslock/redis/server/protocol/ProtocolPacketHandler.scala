package com.capslock.redis.server.protocol

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import com.capslock.redis.command.{Command, RespCommand}

/**
  * Created by capsl on 2016/2/7.
  */


final class ProtocolPacketHandler(session: ActorRef)(implicit system: ActorSystem)
  extends GraphStage[FlowShape[Command, RespCommand]] {

  private val in = Inlet[Command]("in")
  private val out = Outlet[RespCommand]("out")

  override def shape = FlowShape(in, out)


  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {

    setHandler(in, new InHandler {
      override def onPush(): Unit = {
        val command = grab(in)
        println(s"receive command $command")
        session ! command
        pull(in)
      }
    })

    setHandler(out, new OutHandler {
      override def onPull(): Unit = pull(in)
    })
  }
}

