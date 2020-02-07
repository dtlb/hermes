// @formatter:off
lazy val Version = new {
  val jnats          = "2.6.6"
  val jnatsStreaming = "2.2.3"
  val scalaTest      = "3.0.5"
  val fs2 = "1.0.5"
  val log4s = "1.8.2"
}
// @formatter:on

lazy val nexus = project.in(file("."))
  .settings(
    name := "hermes",
    fork in Test := true,
    parallelExecution in Test := false,

    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % Version.scalaTest % Test,
      "io.nats" % "jnats" % Version.jnats,
      "io.nats" % "java-nats-streaming" % Version.jnatsStreaming,
      "co.fs2" %% "fs2-core" % Version.fs2,
      "org.log4s" %% "log4s" % Version.log4s
    )
  )
