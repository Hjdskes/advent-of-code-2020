import Keys._
import sbt.addCompilerPlugin

lazy val root =
  project
    .in(file("."))
    .settings(name := "Advent of Code")
    .settings(moduleName := "advent-of-code")
    .aggregate(day1, day2, day3)

lazy val day1 =
  project
    .in(file("days/one"))
    .settings(commonSettings ++ dependencies)
    .dependsOn(lib)

lazy val day2 =
  project
    .in(file("days/two"))
    .settings(commonSettings ++ dependencies)
    .dependsOn(lib)

lazy val day3 =
  project
    .in(file("days/three"))
    .settings(commonSettings ++ dependencies)
    .dependsOn(lib)

lazy val day4 =
  project
    .in(file("days/four"))
    .settings(commonSettings ++ dependencies)
    .settings(testFrameworks += new TestFramework("munit.Framework"))
    .dependsOn(lib)

lazy val day5 =
  project
    .in(file("days/five"))
    .settings(commonSettings ++ dependencies)
    .dependsOn(lib)

lazy val day6 =
  project
    .in(file("days/six"))
    .settings(commonSettings ++ dependencies)
    .dependsOn(lib)

lazy val lib =
  project
    .in(file("lib"))
    .settings(commonSettings ++ dependencies)

lazy val dependencies = Seq(
  libraryDependencies ++= Seq(
    "co.fs2" %% "fs2-core" % "2.4.4",
    "co.fs2" %% "fs2-io" % "2.4.4",
    "io.estatico" %% "newtype" % "0.4.3",
    "org.scalacheck" %% "scalacheck" % "1.14.3",
    "org.scalameta" %% "munit" % "0.7.19",
    "org.scalameta" %% "munit-scalacheck" % "0.7.19",
    "org.typelevel" %% "cats-core" % "2.1.1",
    "org.typelevel" %% "cats-effect" % "2.1.4",
    "org.typelevel" %% "cats-parse" % "0.1.0"
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
