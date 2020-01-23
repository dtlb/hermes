// @formatter:off
lazy val Version = new {
  val jnats =         "2.6.6"
  val compat =        "0.9.0"

}
// @formatter:on

lazy val nexus = project.in(file("."))
  .settings(
    name := "hermes",
    fork in Test := true,
    parallelExecution in Test := false,

    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.5" % Test,
      "io.nats" % "jnats" % Version.jnats,
      "org.scala-lang.modules" %% "scala-java8-compat" % Version.compat
    )
  )
