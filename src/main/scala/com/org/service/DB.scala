package com.org.service

import com.twitter.finagle.Postgres
import com.typesafe.config.ConfigFactory

object DB {
  val config = ConfigFactory.load()

  val client = Postgres.Client()
    .withCredentials(config.getString("database.user"), Some(config.getString("database.password")))
    .database(config.getString("database.name"))
    .withSessionPool.maxSize(config.getInt("database.numThreads"))
    .withBinaryResults(true)
    .withBinaryParams(true)
    //    .withTransport.tls("host")
    .newRichClient(s"${config.getString("database.host")}:${config.getString("database.port")}")

}