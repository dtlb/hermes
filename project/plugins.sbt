resolvers += "Datalogue sbt release artifactory" at "https://datalogue.jfrog.io/datalogue/sbt-plugins-release"
credentials += Credentials("Artifactory Realm", "datalogue.jfrog.io",
  sys.env.getOrElse("ARTIFACTORY_USERNAME", sys.error(s"ARTIFACTORY_USERNAME is not defined in your environment properties")),
  sys.env.getOrElse("ARTIFACTORY_PASSWORD", sys.error(s"ARTIFACTORY_PASSWORD is not defined in your environment properties"))
)

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")
addSbtPlugin("io.datalogue" % "sbt-datalogue-settings" % "0.3.0")
addSbtPlugin("io.datalogue" % "sbt-datalogue-docker" % "0.5.1")
addSbtPlugin("io.datalogue" % "sbt-datalogue-release" % "0.1.0")
addSbtPlugin("org.xerial.sbt" % "sbt-pack" % "0.11")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.2")
