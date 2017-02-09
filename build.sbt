name := "philosophers_problem"

version := "1.0"

scalaVersion := "2.12.1"

val scalaTestV = "3.0.1"
val akkaV = "2.4.16"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaV,
  "org.scalatest" %% "scalatest" % scalaTestV % "test",
  "com.typesafe.akka" %% "akka-testkit" % akkaV % "test"
)