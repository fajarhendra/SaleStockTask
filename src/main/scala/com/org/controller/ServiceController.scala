package com.org.controller

import java.util.UUID

import com.google.inject.{Inject, Provider}
import com.twitter.finagle.http.{Request, Status}
import com.twitter.inject.Logging
import com.twitter.util.Future

class ServiceController @Inject()(subject: Provider[Option[UUID]]) extends Controller(subject) with Logging {
  get("/load/inventory") { request: Request =>
    val rslt = Map("a" -> 1,"b" -> 2,"c" -> 3)
    toResponse(Future(rslt), response, Status.Ok)
  }

}
