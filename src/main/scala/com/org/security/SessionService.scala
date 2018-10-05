package com.org.security

import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

import com.org.controller.Credentials
import com.org.error.UnauthorizedError
import com.org.service.UserService
import com.twitter.finagle.http.{Cookie => FinatraCookie}
import com.twitter.util.{Duration, Future}

case class SessionID(value: String = UUID.randomUUID().toString) extends AnyVal

case class EncryptedSessionCookie(value: String) extends AnyVal

class SessionService @Inject()(crypto: Crypto,
                               passwordCrypto: PasswordCrypto,
                               sessionStore: SessionStore,
                               userService: UserService) {
  /**
    * Unpacks the user ID from the [[EncryptedSessionCookie]]
    * Checks whether the session cookie has been tampered with
    *
    * @param sessionCookie contains both the hmac and session ID: 'hmac|sessionId'
    * @return the session ID if the encrypted content is valid and untampered,
    *         otherwise [[None]]
    */
  def resolveUserFromSession(sessionCookie: EncryptedSessionCookie): Option[UUID] = {
    for {
      sessionID <- extractSessionID(sessionCookie)
      userId <- sessionStore.resolveUser(sessionID)
    } yield userId
  }

  private def extractSessionID(encryptedSessionCookie: EncryptedSessionCookie): Option[SessionID] = {
    crypto.decrypt(encryptedSessionCookie.value).flatMap { unencryptedContent =>
      if (unencryptedContent.contains("|")) {
        val Array(hmac, sessionId) = unencryptedContent.split("\\|")
        val hmacFromSecret = SecurityUtils.generateHmac(sessionId)
        if (hmac == hmacFromSecret) Some(SessionID(sessionId)) else None
      } else {
        None
      }
    }
  }

  def logout(cookie: EncryptedSessionCookie): Unit = {
    extractSessionID(cookie).foreach(sessionID => sessionStore.remove(sessionID))
  }

  def login(credentials: Credentials): Future[EncryptedSessionCookie] = {
    userService.findByUsername(credentials.username).flatMap { optUser =>
      optUser.map(user => {
        if (passwordCrypto.checkPassword(user.password, credentials.password)) {
          val (cookieContent, sessionID) = createSession()
          sessionStore.put(sessionID, user.id)
          Future.value(cookieContent)
        } else {
          Future.exception(UnauthorizedError(failureMessage(credentials)))
        }
      }).getOrElse(Future.exception(UnauthorizedError(failureMessage(credentials))))
    }
  }

  private def failureMessage(credentials: Credentials) = s"Invalid credentials for user=${credentials.username} pass=${credentials.password}"

  /**
    * Creates a new session.  The session cookie content is hmac|session_id,
    * to prevent tampering client side. The content is then encrypted so the
    * content cannot be interpreted client side.
    *
    * @return a tuple with the cookie to give to the user and the session id, which
    *         can be used to relate future requests to an actual user session
    */
  def createSession(): (EncryptedSessionCookie, SessionID) = {
    val sessionID = SessionID()
    val hmac = SecurityUtils.generateHmac(sessionID.value)
    val encryptedCookie = crypto.encrypt(s"$hmac|${sessionID.value}")
    (EncryptedSessionCookie(encryptedCookie), sessionID)
  }
}

object Sessions {
  val SessionIdName = "sid"

  val ExpiredSessionCookie = new FinatraCookie(SessionIdName, "")
  ExpiredSessionCookie.httpOnly = true
  ExpiredSessionCookie.maxAge = Duration(-10, TimeUnit.DAYS)
}