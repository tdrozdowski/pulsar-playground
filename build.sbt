ThisBuild / scalaVersion := "2.13.6"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "dev.xymox"
ThisBuild / organizationName := "xymox"

val zioVersion     = "1.0.12"
val zioJsonVersion = "0.2.0-M1"

val pulsar4sVersion = "2.7.3"

resolvers += "Sonatype OSS Snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots"
resolvers += "Moar Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

lazy val root = (project in file("."))
  .settings(
    name := "pulsar-playground",
    libraryDependencies ++= Seq(
      "dev.zio"               %% "zio"          % zioVersion,
      "dev.zio"               %% "zio-json"     % zioJsonVersion,
      "com.sksamuel.pulsar4s" %% "pulsar4s-zio" % pulsar4sVersion,
      "dev.zio"               %% "zio-test"     % "1.0.12" % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
