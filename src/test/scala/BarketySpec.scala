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
      val jid = JID("troutwine@jabber.org/helpful")
      jid.username should be === "troutwine"
      jid.domain should be === "jabber.org"
      jid.resource should be === Some("helpful")
    }

    it("should extract partial JID components") {
      val jid = JID("troutwine@jabber.org")
      jid.username should be === "troutwine"
      jid.domain should be === "jabber.org"
      jid.resource should be === None
    }

    it("should convert to implict string") {
      val jid:String = JID("troutwine@jabber.org/helpful")
      jid should be === "troutwine@jabber.org/helpful"
    }

    it("should reject nonsense") {
      intercept[RuntimeException] {
        JID("hihowareyou?")
      }
    }
  }

}

