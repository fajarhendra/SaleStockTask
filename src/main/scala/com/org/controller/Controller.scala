package com.org.controller

import java.util.UUID

import com.google.inject.Provider
import com.org.error.{ServerError, UnauthorizedError}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.finatra.http.response.ResponseBuilder
import com.twitter.finatra.http.{Controller => TwitterController}
import com.twitter.util.Future

class Controller(subject: Provider[Option[UUID]]) extends TwitterController {

  def requireUser(f: UUID => Future[Response]) = {
    subject.get().map { user =>
      f.apply(user)
    } getOrElse {
      response.unauthorized
    }.toFuture
  }

  def requireAdmin(f: UUID => Future[Response]) = ???

  def toResponse(outcome: Future[_], responseBuilder: ResponseBuilder, status: Status = Status.Ok): Future[Response] = {
    outcome flatMap { result =>
      status match {
        case Status.Ok => responseBuilder.ok(result).toFuture
        case Status.Created => responseBuilder.created(result).toFuture
        case _ => responseBuilder.internalServerError.toFuture
      }
    } rescue {
      case UnauthorizedError(s) =>
        debug(s)
        responseBuilder.unauthorized.toFutureException
      case ServerError(s, e) =>
        error(s, e)
        responseBuilder.internalServerError.toFutureException
    }
  }


//  import com.twitter.finatra.ContentType
//
//  object BodyParser {
//
//    private def contentType(request: com.twitter.finagle.http.Request) = {
//      request.contentType.map(_.takeWhile(c => c != ';'))
//        .flatMap(ContentType(_))
//        .getOrElse(new All)
//      request.contentType.flatMap(ContentType(_)).getOrElse(new All)
//    }
//
//    private def asJson(body: String): Option[JValue] =
//      Try(parse(body)).toOption.orElse(Some(JNothing))
//
//    def apply(request: Request): Option[AnyRef] = contentType(request) match {
//      case _: Json => asJson(request.contentString)
//      case _ => None
//    }
//  }

}
