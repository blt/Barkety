import org.scalatest.{Spec,BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers

import akka.actor.Actor._
import akka.util.duration._
import akka.testkit.TestKit
import java.util.concurrent.TimeUnit
import akka.actor.{ActorRef, Actor}

import us.troutwine.barkety.JID

class BarketySpec extends Spec with ShouldMatchers {

  describe("The JID extractor") {

    it("should extract the full-JID components") {
      "troutwine@jabber.org/helpful" match {
        case JID(username:String,domain:String,resource:String) =>
          username should be === "troutwine"
          domain should be === "jabber.org"
          resource should be === "helpful"
      }
    }
    it("should extract partial JID components") {
      "troutwine@jabber.org" match {
        case JID(username:String,domain:String) =>
          username should be === "troutwine"
          domain should be === "jabber.org"
      }
    }
    it("should reject nonsense") {
      JID.unapplySeq("hihowareyou?") should be ('empty)
    }
    it("should have inverse properties") {
      JID.apply( (JID.unapplySeq("troutwine@jabber.org")).get ) should be === "troutwine@jabber.org"
      JID.apply( (JID.unapplySeq("troutwine@jabber.org/")).get ) should be === "troutwine@jabber.org"
      JID.apply( (JID.unapplySeq("troutwine@jabber.org/happy")).get ) should be === "troutwine@jabber.org/happy"
    }
  }

}

