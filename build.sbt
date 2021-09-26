name := "Tickets4Sale"

version := "0.1"

scalaVersion := "2.12.4"

val akkaHttpVersion = "10.2.6"

libraryDependencies ++= {
  Seq(
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    "joda-time" % "joda-time" % "2.10.10",

    "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test",

    "org.scalatest" %% "scalatest" % "3.2.9" % "test",

    "com.typesafe" % "config" % "1.4.1",

    "com.typesafe.akka" % "akka-actor-typed_2.12" % "2.6.8",

    "com.typesafe.akka" %% "akka-stream" % "2.6.8",

    "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"




  )
}