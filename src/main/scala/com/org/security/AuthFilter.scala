package com.org.security

import java.util.UUID

import com.google.inject.Inject
import com.twitter.finagle.http.{CookieMap, Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.inject.requestscope.FinagleRequestScope
import com.twitter.util.Future

class AuthFilter @Inject()(requestScope: FinagleRequestScope,
                           sessionService: SessionService) extends SimpleFilter[Request, Response] {

  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    val userID = resolveUserFromRequest(request)
    requestScope.seed[Option[UUID]](userID)
    service(request)
  }

  private def resolveUserFromRequest(request: Request): Option[UUID] =
    for {
      encryptedCookie <- extractCookieFromRequest(request.cookies)
      userID <- sessionService.resolveUserFromSession(encryptedCookie)
    } yield userID

  private def extractCookieFromRequest(cookies: CookieMap): Option[EncryptedSessionCookie] =
    cookies.get(Sessions.SessionIdName).map(cookie => EncryptedSessionCookie(cookie.value))
}
