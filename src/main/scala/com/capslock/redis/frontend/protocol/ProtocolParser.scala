package com.capslock.redis.frontend.protocol

import akka.actor.ActorSystem
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import com.capslock.redis.command.{Command, NoneRequestCommand, RequestCommand}

import scala.util.control.NoStackTrace

/**
  * Created by capsl on 2016/2/7.
  */


final class ProtocolParser(implicit system: ActorSystem)
  extends GraphStage[FlowShape[String, Command]] {

  private val in = Inlet[String]("in")
  private val out = Outlet[Command]("out")

  override def shape = FlowShape(in, out)

  sealed trait ParseState

  @SerialVersionUID(1L)
  case object AwaitArgumentCount extends ParseState

  @SerialVersionUID(1L)
  case class AwaitArgumentDataLength(argumentCount: Int, argumentDataList: List[String]) extends ParseState

  @SerialVersionUID(1L)
  case class AwaitArgumentData(argumentCount: Int, argumentDataList: List[String], argumentDataLength: Int) extends ParseState

  @SerialVersionUID(1L)
  case class FailedState(msg: String) extends ParseState

  @inline
  private def failedState(msg: String) = (FailedState(msg), NoneRequestCommand)

  def toInt(number: String): Option[Integer] = {
    try {
      Some(number.toInt)
    } catch {
      case e: Exception => None
    }
  }


  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    private[this] var parserState: ParseState = AwaitArgumentCount

    setHandler(in, new InHandler {
      override def onPush(): Unit = {
        val chunk = grab(in)
        val (newState, command) = doParse(parserState, chunk)
        newState match {
          case FailedState(msg) =>
            println("Failed to parse connection-level {}", msg)
            failStage(new Exception(msg) with NoStackTrace)
          case _ =>
            parserState = newState
            if (!command.eq(NoneRequestCommand)) {
              emit(out, command)
            } else {
              pull(in)
            }

        }
      }
    })

    setHandler(out, new OutHandler {
      override def onPull(): Unit = pull(in)
    })

    private def doParse(state: ParseState, commandData: String): (ParseState, Command) = {
      state match {
        case AwaitArgumentCount =>
          if (commandData.startsWith("*")) {

            toInt(commandData.substring(1)) match {
              case Some(argumentCount) => (AwaitArgumentDataLength(argumentCount, List()), NoneRequestCommand)
              case _ => failedState("parse argument count failed")
            }

          } else {
            failedState("argument count command must start with *")
          }
        case AwaitArgumentDataLength(argumentCount, argumentDataList) =>
          if (commandData.startsWith("$")) {

            toInt(commandData.substring(1)) match {
              case Some(dataLength) => (AwaitArgumentData(argumentCount, argumentDataList, dataLength), NoneRequestCommand)
              case _ => failedState("parse argument length failed")
            }

          } else {
            failedState("argument data command must start with $")
          }
        case AwaitArgumentData(argumentCount, argumentDataList, argumentDataLength) =>
          if (commandData.length == argumentDataLength) {
            val newDataList = argumentDataList ::: List(commandData)

            if (argumentCount == newDataList.length) {
              (AwaitArgumentCount, RequestCommand(newDataList))
            } else {
              (AwaitArgumentDataLength(argumentCount, newDataList), NoneRequestCommand)
            }

          } else {
            failedState("argument data length miss match")
          }
      }
    }
  }
}

