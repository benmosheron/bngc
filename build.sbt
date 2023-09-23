name := "BallisticNG Campaign Generator (BNGC)"

version := "1.0"

scalaVersion := "2.13.10"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % "2.2.0",
  "org.typelevel" %% "cats-core" % "2.10.0",
  "org.typelevel" %% "cats-effect" % "3.5.1",
  "co.fs2" %% "fs2-core" % "3.9.2",
  "co.fs2" %% "fs2-io" % "3.9.2"
)
