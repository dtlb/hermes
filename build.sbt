// @formatter:off
lazy val Version = new {
  val jnats          = "2.6.6"
  val jnatsStreaming = "2.2.3"
  val scalaTest      = "3.0.5"
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
      "io.nats" % "java-nats-streaming" % Version.jnatsStreaming
    )
  )
