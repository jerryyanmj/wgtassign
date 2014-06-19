name := """wgtassign"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala).enablePlugins(SbtTwirl)

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  cache,
  filters,
  "postgresql" % "postgresql" % "9.1-901.jdbc4",
  "com.typesafe.play" %% "play-slick" % "0.7.0-M1"
)
