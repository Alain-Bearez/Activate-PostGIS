name := "ActivatePostGis"

organization  := "cua.li"

version       := "0.1.0"

scalaVersion  := "2.11.7"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8")

resolvers ++= Seq(
//"Activate repository" at "http://fwbrasil.net/maven/",
//"Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
//"Local Maven Repository" at "" + Path.userHome.asFile.toURI.toURL + "/.m2/repository",
  "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"
)

libraryDependencies ++= {
  val activateV = "1.7"
  Seq(
    "io.spray"            %%  "spray-json"          % "1.3.2"            withSources() withJavadoc(),
    "joda-time"           %   "joda-time"           % "2.8.1"            withSources() withJavadoc(),
    "net.fwbrasil"        %%  "activate-core"       % activateV          withSources() withJavadoc(),
    "net.fwbrasil"        %%  "activate-spray-json" % activateV          withSources() withJavadoc(),
    "net.fwbrasil"        %%  "activate-jdbc"       % activateV          withSources() withJavadoc(),
    "net.fwbrasil"        %%  "activate-jdbc-async" % activateV          withSources() withJavadoc(),
    "org.postgresql"      %   "postgresql"          % "9.4-1201-jdbc41"  withSources() withJavadoc(),
    "ch.qos.logback"      %   "logback-classic"     % "1.1.3"
  )
}

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource
