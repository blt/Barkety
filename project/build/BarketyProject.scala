import sbt._

class BarketyProject(info: ProjectInfo) extends DefaultProject(info) with AkkaProject {
  val akkaModuleConfig = ModuleConfiguration("se.scalablesolutions.akka", AkkaRepositories.Akka_Repository)
  val akkaTestKit = akkaModule("testkit")

  // Bundled owing to lack of jars in any public repository.
  // val smack = "jivesoftware" % "smack" % "3.2.0"
  // val smackx= "jivesoftware" % "smackx" % "3.2.0"

  val scalaTest = "org.scalatest" %% "scalatest" % "1.4.1"

  override def managedStyle = ManagedStyle.Maven
  lazy val publishTo = Resolver.sftp("troutwine.us repository", "maven.troutwine.us", "/srv/http/us/troutwine/maven/")
}
