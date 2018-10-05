package com.org.controller

import java.util.UUID
import javax.inject.Inject

import com.google.inject.Provider
import com.org.error.UnauthorizedError
import com.org.security.SecurityUtils.credentialsFromAuthHeader
import com.org.security.{EncryptedSessionCookie, SessionService, Sessions}
import com.org.service.UserService
import com.twitter.concurrent.AsyncStream
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finatra.request.Header
import com.twitter.inject.Logging
import com.twitter.io.{Buf, Reader}
import com.twitter.util.{Future, JavaTimer, Try}
import org.jboss.netty.handler.codec.http.DefaultCookie

import scala.util.Random
import com.twitter.conversions.time._
import com.twitter.finatra.http.response.StreamingResponse
import com.twitter.finatra.json.FinatraObjectMapper

import scala.reflect.io.File

class UserController @Inject()(userService: UserService,
                               sessionService: SessionService,
                               subject: Provider[Option[UUID]]) extends Controller(subject) with Logging {

  val random = new Random
  implicit val timer = new JavaTimer
  val mapper = FinatraObjectMapper.create()

  // Int, sleep, repeat.
  def ints(): AsyncStream[Int] = random.nextInt +:: AsyncStream.fromFuture(Future.sleep(100.millis)).flatMap(_ => ints())


  get("/stream") { request: Request =>
//    ints()
    val fileName = "/Users/subhan/Downloads/test-stream.txt"
    import java.io.File
    val asyncStream = AsyncStream.fromReader(Reader.fromFile(new File(fileName))).map(buf =>Buf.Utf8.unapply(buf).getOrElse(""))

//    import scala.io.Source
//    val asyncStream = AsyncStream.fromSeq(Source.fromFile(fileName).getLines.toSeq)

    val streamingResponse = StreamingResponse.jsonArray(
      toBuf = mapper.writeValueAsBuf,
      asyncStream = asyncStream)

    streamingResponse.toFutureFinagleResponse
  }

  get("simple") { request: Request =>
    ints()
  }

  get("/hello") { request: Request =>
    println("get hello")
    "hello"
  }

  get("/aggrid") { request: Request =>
    println("ag-grid")
    ints().foreach(x => println)
    // Only one stream exists.
    //    var messages: AsyncStream[Buf] = ints().map(n => Buf.Utf8(n.toString))
    var messages: AsyncStream[Buf] = ints().map(n => Buf.Utf8("{name:\"Sophie Beckham -  518\"}"))
    //    var messages: AsyncStream[Buf] = ints().map(n => Buf.Utf8("{\"name\":\"Sophie Beckham - " + n.toString +"\",\"skills\":{\"android\":false,\"html5\":true,\"mac\":true,\"windows\":false,\"css\":false},\"address\":\"1197 Thunder Wagon Common, Cataract, RI, 02987-1016, US, (401) 747-0763\",\"years\":76,\"proficiency\":81,\"country\":\"Ireland\",\"continent\":\"Europe\",\"language\":\"English\",\"mobile\":\"+186 739 890 695\",\"landline\":\"+587 642 631 518\"}"))
    // Allow the head of the stream to be collected.
    messages.foreach(_ => messages = messages.drop(1))
    val writable = Reader.writable()
    // Start writing thread.
    messages.foreachF(writable.write)
    val response = Response(request.version, Status.Ok, writable)
    response.setChunked(true)
    Future.value(response)
  }

  get("/user") { request: Request =>
//    requireUser { user =>
      toResponse(userService.users(), response)
//    }

  }

//  post("/login") { request: Request =>
//    println(s"login dipanggil 1 -> $request")
//    Status.Accepted
//  }

  get("/post") { request: Request =>
    println(s"post dipanggil 1 -> $request")
  }


    options("/login") { request: Request =>
        println(s"login dipanggil 0 -> $request")
      Status.Ok
  }
//
  post("/login") { request: LoginRequest =>
    println(s"login dipanggil 2 -> $request")
    credentialsFromAuthHeader(request.Authorization).map { credentials => {
      sessionService.login(credentials).map { sessionID =>
        val cookie = new DefaultCookie(Sessions.SessionIdName, sessionID.value)
        response.ok().cookie(cookie)
      } rescue {
        case UnauthorizedError(message) =>
          debug(message)
          response.unauthorized.toFutureException
        case _ => response.internalServerError.toFutureException
      }
    }
    }.getOrElse(response.unauthorized.toFuture)
  }

  post("/logout") { request: Request =>
    val encryptedCookie = request.cookies.get(Sessions.SessionIdName)
    encryptedCookie.foreach(cookie => sessionService.logout(EncryptedSessionCookie(cookie.value)))
    response.ok().cookie(Sessions.ExpiredSessionCookie).toFuture
  }
}

case class Credentials(username: String, password: String)

case class LoginRequest(@Header Authorization: String)
//case class LoginRequest(@Header Authorization: String, @Header ContentType: String)
