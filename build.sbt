/**
  * Created by: Jacob Bishop on 2019/12/06
  *
  * Herding CATS
  */


ThisBuild / name := "herding-cats"
ThisBuild / useCoursier := false // temporarily disable Coursier
ThisBuild / organization := "com.github.jacobbishopxy"
ThisBuild / scalaVersion := "2.13.3"

javacOptions ++= Seq("-encoding", "UTF-8")
javaOptions in run += "-Xmx1G"


scalacOptions ++= List(
  "-unchecked",
  "-deprecation",
  "-Ymacro-annotations",
  "-language:higherKinds",
  "-language:implicitConversions"
)

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)

val catsV = "2.0.0"
val catsColV = "0.9.0"
val catsEffV = "2.1.4"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % catsV,
  "org.typelevel" %% "cats-macros" % catsV,
  "org.typelevel" %% "cats-kernel" % catsV,
  "org.typelevel" %% "cats-laws" % catsV,
  "org.typelevel" %% "cats-free" % catsV,
  "org.typelevel" %% "simulacrum" % "1.0.0",
  "org.typelevel" %% "cats-collections-core" % catsColV,
  "org.typelevel" %% "cats-effect" % catsEffV,
)

