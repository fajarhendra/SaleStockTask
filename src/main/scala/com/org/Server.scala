package com.org

import com.org.common.json.CustomJacksonModule
import com.org.controller.{ServiceController, UserController}
import com.org.security.{AuthFilter, AuthModule, CorsFilter}
import com.twitter.concurrent.AsyncStream
import com.twitter.finagle.{Service, Websocket}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.filters.{CommonFilters, LoggingMDCFilter, TraceIdMDCFilter}
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.inject.requestscope.FinagleRequestScopeFilter
import com.twitter.util.{Future, JavaTimer}
import com.typesafe.config.ConfigFactory

import scala.util.Random

object ServerMain extends Server

class Server extends HttpServer {
  val config = ConfigFactory.load()

  override def jacksonModule = CustomJacksonModule

  //module dipakai untuk inject pakai Guice
  override def modules = Seq(AppModule, AuthModule)

  override def defaultFinatraHttpPort = s":${config.getString("finatra.port")}"

  override def configureHttp(router: HttpRouter) {
    router
      .filter[LoggingMDCFilter[Request, Response]]
      .filter[TraceIdMDCFilter[Request, Response]]
      .filter[CommonFilters]
      //FinagleRequestScope membuat scope dan bind pada level context finagle
      .filter[FinagleRequestScopeFilter[Request, Response]]
      .filter[AuthFilter]
      .filter[CorsFilter]
      .add[ServiceController]
//      .add[PostController]
  }


  import com.twitter.conversions.time._
  val random = new Random
  implicit val timer = new JavaTimer
  private  def ints(): AsyncStream[Int] =
    random.nextInt +::
      AsyncStream.fromFuture(Future.sleep(1000.millis)).flatMap(_ => ints())


  import com.twitter.finagle.websocket.{Frame, Request, Response}
  val service = new Service[Request, Response] {
    // Only one stream exists.
    @volatile private[this] var messages: AsyncStream[Frame] =
      ints().map(n => Frame.Text("{\"name\":\"Sophie Beckham\",\"skills\":{\"android\":true,\"html5\":true,\"mac\":false,\"windows\":false,\"css\":false},\"address\":\"1197 Thunder Wagon Common, Cataract, RI, 02987-1016, US, (401) 747-0763\",\"years\":43,\"proficiency\":85,\"country\":\"Ireland\",\"continent\":\"Europe\",\"language\":\"English\",\"mobile\":\"+630 553 428 959\",\"landline\":\"+074 904 909 911\"}\n"))


    // Allow the head of the stream to be collected.
    messages.foreach(_ => messages = messages.drop(1))

    def apply(request: Request) = {
      //      val writable = Reader.writable()
      //      // Start writing thread.
      //      messages.foreachF(writable.write)
      //      val cek = AsyncStream.fromReader(writable)
      println(messages)
      Future.value(Response(messages))
    }
  }

  Websocket.serve(":3005", service)
}
