name := "hermes"

version := "0.1"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  "io.nats" % "jnats" % "2.6.6",
  "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.0"
)