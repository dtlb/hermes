import com.typesafe.config.ConfigFactory

name := "hermes"

version := "0.1"
unmanagedResources in Compile += baseDirectory.value / "release" / "version.dtl"

lazy val themis = (project in file("."))
  .settings(
    name := "themis",
    fork in Test := true,
    fork in run := true,

    dockerArtifactoryUsername := sys.env.getOrElse(
      "ARTIFACTORY_USERNAME",
      sys.error("ARTIFACTORY_USERNAME is not defined in your environment properties")),
    dockerArtifactoryPassword := sys.env.getOrElse(
      "ARTIFACTORY_PASSWORD",
      sys.error("ARTIFACTORY_PASSWORD is not defined in your environment properties")),

//    testConfig := ConfigFactory.load(ConfigFactory.parseFile((resourceDirectory in Test).value / "application.conf")),

    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.5" % Test,
      "io.nats" % "jnats" % "2.6.6",
      "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.0"
    ),
    releaseProcess := DatalogueReleasePlugin.datalogueReleaseProcessWithDocker(),

    packJvmOpts := Map(name.value -> Seq(
      "-server",
      "-XX:+UnlockExperimentalVMOptions",
      "-XX:+UseCGroupMemoryLimitForHeap",
      "-XX:MaxRAMFraction=1",
      "-XshowSettings:vm",
      "-XX:+UseG1GC",
      "-XX:+CMSClassUnloadingEnabled",
      "-Dconfig.resource=application.prod.conf",
      "-Dlog4j.configurationFile=log4j2.xml"
    ))
  )
  .enablePlugins(PackPlugin, DatalogueDockerPlugin)