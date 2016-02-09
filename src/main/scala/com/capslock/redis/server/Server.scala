package com.capslock.redis.server

import akka.actor._
import akka.stream._
import akka.stream.actor.ActorPublisher
import akka.stream.io._
import akka.stream.scaladsl._
import akka.stream.stage._
import akka.util._
import com.capslock.redis.CacheManger.CacheManager
import com.capslock.redis.client.ClientSession
import com.capslock.redis.command.response.RESP
import com.capslock.redis.command.{RespCommand, Command}
import com.capslock.redis.frontend.protocol.{ProtocolPacketHandler, ProtocolParser}
import com.capslock.redis.router.CommandRouter

import scala.util._

object Server {

  def logicFlow(conn: Tcp.IncomingConnection, commandRouter: ActorRef, cacheManager: ActorRef)(implicit system: ActorSystem): Flow[ByteString, ByteString, Unit] =
    Flow.fromGraph(GraphDSL.create() { implicit builder ⇒
      import GraphDSL.Implicits._

      val sessionClient = system.actorOf(ClientSession.props(commandRouter, cacheManager))
      val sessionClientSource = Source.fromPublisher(ActorPublisher[Command](sessionClient))
      val session = builder.add(sessionClientSource)

      val mapRespFlow: Flow[Command, ByteString, Unit] = Flow[Command]
        .transform(() => mapResponse(system))

      val packetHandler = builder.add(Flow.fromGraph(new ProtocolParser()).via(new ProtocolPacketHandler(sessionClient)))

      val delimiter = builder.add(Flow[ByteString]
        .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 256, allowTruncation = true))
        .map(_.utf8String)
        .map { msg => msg.substring(0, msg.length - 1) })

      val merge = builder.add(Merge[Command](2))
      val mapResp = builder.add(mapRespFlow)
      delimiter ~> packetHandler ~> merge
      session ~> merge ~> mapResp

      FlowShape(delimiter.in, mapResp.out)
    })

  def mkServer(address: String, port: Int, commandRouter: ActorRef, cacheManager: ActorRef)(implicit system: ActorSystem, materializer: Materializer): Unit = {
    import system.dispatcher

    val connectionHandler = Sink.foreach[Tcp.IncomingConnection] { conn ⇒
      println(s"Incoming connection from: ${conn.remoteAddress}")
      conn.handleWith(logicFlow(conn, commandRouter, cacheManager))
    }
    val incomingConnections = Tcp().bind(address, port)
    val binding = incomingConnections.to(connectionHandler).run()

    binding onComplete {
      case Success(b) ⇒
        println(s"Server started, listening on: ${b.localAddress}")
      case Failure(e) ⇒
        println(s"Server could not be bound to $address:$port: ${e.getMessage}")
    }
  }

  def mapResponse(system: ActorSystem) = new PushStage[Command, ByteString] {
    override def onPush(cmd: Command, ctx: Context[ByteString]) = {
      cmd match {
        case respCommand: RespCommand => ctx.push(ByteString(RESP.encode(respCommand.resp)))
        case _ => onPull(ctx)
      }
    }
  }

  def startServer(address: String, port: Int) = {
    implicit val system = ActorSystem("Server")
    implicit val materializer = ActorMaterializer()
    implicit val commandRouter = system.actorOf(Props[CommandRouter])
    implicit val cacheManager = system.actorOf(Props[CacheManager])

    mkServer(address, port, commandRouter, cacheManager)
  }
}