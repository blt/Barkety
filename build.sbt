organization := "us.troutwine"

name := "barkety"

version := "3.2.0"

scalaVersion := "2.9.0"

scalacOptions ++= Seq("-unchecked", "-deprecation")

resolvers += "Glassfish" at "http://maven.glassfish.org/content/repositories/maven.hudson-labs.org"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "1.6.1",
  "org.mockito" % "mockito-core" % "1.8.5"
  ) map { _ % "test" }

libraryDependencies ++= Seq("actor") map { "se.scalablesolutions.akka" % "akka-%s".format(_) % "1.2" }

libraryDependencies ++= Seq("testkit") map { "se.scalablesolutions.akka" % "akka-%s".format(_) % "1.2" % "test" }

libraryDependencies ++= Seq("smack", "smackx") map { "jivesoftware" % _ % "3.2.0" }

initialCommands := """
import akka.actor._
import akka.actor.Actor._
import us.troutwine.barkety._
"""

//* vim: set filetype=scala : */
