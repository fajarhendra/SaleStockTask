lazy val commonSettings = Seq(
  version := "1.0.0",
  scalaVersion := "2.11.8",
  //  crossScalas := Seq("2.11.8", "2.10.5", "2.9.2"),
  fork in run := true
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "finatra-boilerplate"
  )

resolvers ++= Seq(
  //  "com.github.finagle" at "https://dl.bintray.com/bugagazavr/maven",
  //    "com.github.sprsquish" at "https://raw.github.com/sprsquish/mvn-repo/master",
  "net.postgis" at "https://raw.github.com/postgis/mvn-repo/master",
  Resolver.sonatypeRepo("releases"),
  "Twitter Maven" at "https://maven.twttr.com",
  "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
  "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/",
  "ElasticSearch Repo" at "http://maven.elasticsearch.org/public-releases/",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "maven repo" at "https://mvnrepository.com/artifact/"
)

javaOptions ++= Seq(
  "-Dlog.service.output=/dev/stderr",
  "-Dlog.access.output=/dev/stderr")

(testOptions in Test) += Tests.Argument("-h", "target/html-test-report", "-o")

lazy val versions = new {
  val finatra = "2.8.0"
  val guice = "4.0"
  val logback = "1.1.2"
  val mockito = "1.9.5"
  val postgres = "42.0.0"
  val postgis = "2.2.1"
  //  val redis = "3.0"
  val scalaCache = "0.9.3"
  val json4s = "3.5.0"
  val swagger = "0.7.2"
  val scalatest = "2.2.3"
  val specs2 = "2.3.12"
  val typesafeConfig = "1.3.0"
  val lib = "6.34.0"
  val jackson = "2.8.7"
  val elastic4s = "5.2.2"
  val akka4s = "2.4.1"
  val finaglePostgres = "0.4.1"
}


libraryDependencies ++= Seq(

  //finatra
  "com.twitter" %% "finatra-http" % versions.finatra,
  "com.twitter" %% "inject-request-scope" % versions.finatra,
//  "com.twitter" %% "finatra-slf4j" % versions.finatra,
  "com.twitter" %% "finagle-stats" % versions.finatra,

  //enkripsi
  "com.github.t3hnar" %% "scala-bcrypt" % "2.5",
  "org.jasypt" % "jasypt" % "1.9.2",

  //common
  "ch.qos.logback" % "logback-classic" % versions.logback,
  "com.typesafe" % "config" % versions.typesafeConfig,

  //json
  "org.json4s" %% "json4s-ast" % versions.json4s,
  "org.json4s" %% "json4s-core" % versions.json4s,
  "org.json4s" %% "json4s-native" % versions.json4s % "test",

  //websocket
  //outdated
  //  "com.github.finagle" %% "finagle-websocket" % "6.26.0",
  //  https://raw.github.com/sprsquish/mvn-repo/master/com/github/sprsquish/finagle-websockets_2.10/6.8.1/finagle-websockets_2.10-6.8.1.jar
  //  "com.github.sprsquish" % "finagle-websockets_2.10" % "6.8.1",

  //xml parser
  "com.fasterxml.jackson.core" % "jackson-core" % versions.jackson,
  "com.fasterxml.jackson.core" % "jackson-annotations" % versions.jackson,
  "com.fasterxml.jackson.core" % "jackson-databind" % versions.jackson,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % versions.jackson,

  //supaya tidak error memakai elasticsearch 5.2.x
  //  "com.fasterxml.jackson.dataformat" % "jackson-dataformat-cbor" % versions.jackson,


  //database
  "io.github.finagle" %% "finagle-postgres" % versions.finaglePostgres,
  "org.postgresql" % "postgresql" % versions.postgres,
  "net.postgis" % "postgis-jdbc" % versions.postgis,
  //  "net.debasishg" %% "redisclient" % versions.redis,

  //cache
  "com.github.cb372" %% "scalacache-core" % versions.scalaCache,
  //  "com.github.cb372" % "scalacache-redis_2.12.0-RC1" % "0.9.2",
  "com.github.cb372" %% "scalacache-ehcache" % versions.scalaCache,


  //elastic4s
  //  "com.sksamuel.elastic4s" %% "elastic4s-core" % versions.elastic4s,
  //  "com.sksamuel.elastic4s" %% "elastic4s-tcp" % versions.elastic4s,
  //  "com.sksamuel.elastic4s" %% "elastic4s-jackson" % versions.elastic4s,
  //  "org.elasticsearch.module" % "reindex" % versions.elastic4s,
  //  "net.java.dev.jna" % "jna" % "4.2.1",
  //  "com.vividsolutions" % "jts" % "1.13",
  //  "com.spatial4j" % "spatial4j" % "0.5",

  //spark
  "org.apache.spark" %% "spark-sql" % "2.2.0",
"com.crealytics" %% "spark-excel" % "0.9.17",

  //excel
  "org.apache.poi" % "poi" % "3.13",
  "com.norbitltd" % "spoiwo" % "1.0.6",
  "com.monitorjbl" % "xlsx-streamer" % "0.2.10",


  //akka
  //  "com.typesafe.akka" %% "akka-actor" % versions.akka4s,


  //swagger
  "com.github.xiaodongw" %% "swagger-finatra" % versions.swagger,

  //test
//  "ch.qos.logback" % "logback-classic" % versions.logback % "test",
  "com.twitter" %% "finatra-http" % versions.finatra % "test",
  "com.twitter" %% "finatra-jackson" % versions.finatra % "test",
  "com.twitter" %% "inject-server" % versions.finatra % "test",
  "com.twitter" %% "inject-app" % versions.finatra % "test",
  "com.twitter" %% "inject-core" % versions.finatra % "test",
  "com.twitter" %% "inject-modules" % versions.finatra % "test",
  "com.google.inject.extensions" % "guice-testlib" % versions.guice % "test",
  "com.twitter" %% "finatra-http" % versions.finatra % "test" classifier "tests",
  "com.twitter" %% "inject-server" % versions.finatra % "test" classifier "tests",
  "com.twitter" %% "inject-app" % versions.finatra % "test" classifier "tests",
  "com.twitter" %% "inject-core" % versions.finatra % "test" classifier "tests",
  "com.twitter" %% "inject-modules" % versions.finatra % "test" classifier "tests",
  "com.ninja-squad" % "DbSetup" % "1.6.0" % "test",
  "org.mockito" % "mockito-core" % versions.mockito % "test",
  "org.scalatest" %% "scalatest" % versions.scalatest % "test",
  "org.specs2" %% "specs2" % versions.specs2 % "test"
)


