package com.twitter.finagle.websocket

import com.twitter.finagle.Stack
import com.twitter.finagle.client.Transporter
import com.twitter.finagle.netty3._
import com.twitter.finagle.server.Listener
import java.net.URI
import org.jboss.netty.channel.{Channels, ChannelPipelineFactory}
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.handler.codec.http.websocketx._
import scala.collection.JavaConverters._

private[finagle] object Netty3 {
  import Frame._

  private def serverPipeline = {
    val pipeline = Channels.pipeline()
    pipeline.addLast("decoder", new HttpRequestDecoder)
    pipeline.addLast("encoder", new HttpResponseEncoder)
    pipeline.addLast("handler", new WebSocketServerHandler)
    pipeline
  }

  private def clientPipeline() = {
    val pipeline = Channels.pipeline()
    pipeline.addLast("decoder", new HttpResponseDecoder)
    pipeline.addLast("encoder", new HttpRequestEncoder)
    pipeline.addLast("handler", new WebSocketClientHandler)
    pipeline
  }

  def newListener[In, Out](params: Stack.Params): Listener[In, Out] =
    Netty3Listener(new ChannelPipelineFactory {
      def getPipeline() = serverPipeline
    }, params)

  def newTransporter[In, Out](params: Stack.Params): Transporter[In, Out] = {
    Netty3Transporter(new ChannelPipelineFactory {
      def getPipeline() = clientPipeline()
    }, params)
  }

  def fromNetty(m: Any): Frame = m match {
    case text: TextWebSocketFrame =>
      Text(text.getText)

    case cont: ContinuationWebSocketFrame =>
      Text(cont.getText)

    case bin: BinaryWebSocketFrame =>
      Binary(new ChannelBufferBuf(bin.getBinaryData))

    case ping: PingWebSocketFrame =>
      Ping(new ChannelBufferBuf(ping.getBinaryData))

    case pong: PongWebSocketFrame =>
      Pong(new ChannelBufferBuf(pong.getBinaryData))

    case frame =>
      throw new IllegalStateException(s"unknown frame: $frame")
  }

  def toNetty(frame: Frame): WebSocketFrame = frame match {
    case Text(message) =>
      new TextWebSocketFrame(message)

    case Binary(buf) =>
      new BinaryWebSocketFrame(BufChannelBuffer(buf))

    case Ping(buf) =>
      new PingWebSocketFrame(BufChannelBuffer(buf))

    case Pong(buf) =>
      new PongWebSocketFrame(BufChannelBuffer(buf))
  }

  def newHandshaker(uri: URI, headers: Map[String, String]): WebSocketClientHandshaker = {
    val factory = new WebSocketClientHandshakerFactory
    factory.newHandshaker(uri, WebSocketVersion.V13, null, false, headers.asJava)
  }
}
