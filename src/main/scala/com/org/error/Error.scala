package com.org.error

abstract class Error(val reason: String, val cause: Throwable) extends Exception(reason, cause)

final case class BadCredentialsError(override val reason: String,
                                     override val cause: Throwable) extends Error(reason, cause)

final case class NotFoundError(override val reason: String) extends Error(reason, null)

final case class UnauthorizedError(override val reason: String) extends Error(reason, null)

final case class AuthenticationError(override val reason: String,
                                     override val cause: Throwable) extends Error(reason, cause)

final case class ServerError(override val reason: String,
                             override val cause: Throwable) extends Error(reason, cause)

final case class BadSessionCookieError(override val reason: String,
                                       override val cause: Throwable) extends Error(reason, cause)
