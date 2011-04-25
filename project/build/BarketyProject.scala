import sbt._

class BarketyProject(info: ProjectInfo) extends DefaultProject(info) with AkkaProject {
  val akkaModuleConfig = ModuleConfiguration("se.scalablesolutions.akka", AkkaRepositories.Akka_Repository)
  val akkaTestKit = akkaModule("testkit")

  val smack = "jivesoftware" % "smackx" % "3.1.0"
  val smackx= "jivesoftware" % "smackx" % "3.1.0"

  val scalaTest = "org.scalatest" % "scalatest" % "1.4-SNAPSHOT"
}
