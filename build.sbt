/**
  * Created by: Jacob Bishop on 2019/12/06
  *
  * Herding CATS
  */


ThisBuild / name := "herding-cats"
ThisBuild / useCoursier := false // temporarily disable Coursier
ThisBuild / organization := "com.github.jacobbishopxy"
ThisBuild / scalaVersion := "2.13.1"

javacOptions ++= Seq("-encoding", "UTF-8")
javaOptions in run += "-Xmx1G"


scalacOptions ++= List(
  "-unchecked",
  "-deprecation",
  "-Ymacro-annotations",
  "-language:higherKinds",
  "-language:implicitConversions"
)

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.0.0",
  "org.typelevel" %% "simulacrum" % "1.0.0",
)

