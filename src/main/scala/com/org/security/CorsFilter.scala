package com.org.security

import com.google.inject.Inject
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.inject.requestscope.FinagleRequestScope
import com.twitter.util.Future

/**
  * Application CORS filter.  Currently uses [[Cors.UnsafePermissivePolicy]] but should be
  * more constrained in production.
  */
class CorsFilter @Inject()(requestScope: FinagleRequestScope) extends SimpleFilter[Request, Response] {
  val allowsOrigin  = { origin: String => Some("http://localhost:3000") }
  val allowsMethods = { method: String => Some(Seq("GET", "POST", "PUT", "DELETE", "OPTIONS")) }
  val allowsHeaders = { headers: Seq[String] => Some(headers) }

  val policy = Cors.Policy(allowsOrigin, allowsMethods, allowsHeaders, supportsCredentials = true)
  val cors = new HttpFilter(policy)
//  val cors = new HttpFilter(Cors.UnsafePermissivePolicy)

  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    cors.apply(request, service)
  }
}
