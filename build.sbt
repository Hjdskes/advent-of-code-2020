import Keys._
import sbt.addCompilerPlugin

lazy val root =
  project
    .in(file("."))
    .settings(name := "Advent of Code")
    .settings(moduleName := "advent-of-code")
    .aggregate(day1)

lazy val day1 =
  project
    .in(file("days/one"))
    .settings(commonSettings ++ dependencies)

lazy val dependencies = Seq(
  libraryDependencies ++= Seq(
    "co.fs2" %% "fs2-core" % "2.4.4",
    "co.fs2" %% "fs2-io" % "2.4.4",
    "org.typelevel" %% "cats-core" % "2.1.1",
    "org.typelevel" %% "cats-effect" % "2.1.4"
  )
)

lazy val commonSettings = Seq(
  organization := "nl.hjdskes",
  scalaVersion := "2.13.4",
  scalaSource in Compile := baseDirectory.value / "src",
  scalaSource in Test := baseDirectory.value / "test",
  resourceDirectory in Compile := baseDirectory.value / "resources",
  resourceDirectory in Test := baseDirectory.value / "test" / "resources",
  scalacOptions ++= Seq(
    "-Xfatal-warnings",
    "-Ymacro-annotations"
  ),
  scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value,
  addCompilerPlugin(
    "org.typelevel" %% "kind-projector" % "0.11.2" cross CrossVersion.full
  )
)
