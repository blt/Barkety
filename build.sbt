organization := "us.troutwine"

name := "barkety"

version := "3.2.0"

scalaVersion := "2.9.0"

resolvers += "Glassfish" at "http://maven.glassfish.org/content/repositories/maven.hudson-labs.org"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "1.6.1"
  ) map { _ % "test" }

libraryDependencies ++= Seq("actor") map { "se.scalablesolutions.akka" % "akka-%s".format(_) % "1.2" }

libraryDependencies ++= Seq("testkit") map { "se.scalablesolutions.akka" % "akka-%s".format(_) % "1.2" % "test" }

libraryDependencies ++= Seq("smack", "smackx") map { "jivesoftware" % _ % "3.2.0" }

//* vim: set filetype=scala : */
