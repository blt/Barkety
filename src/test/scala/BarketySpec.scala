import org.scalatest.{Spec,BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers

import akka.actor.Actor._
import akka.util.duration._
import akka.testkit.TestKit
import java.util.concurrent.TimeUnit
import akka.actor.{ActorRef, Actor}

import us.troutwine.barkety.{JID,ChatSupervisor,RegisterParent}
import us.troutwine.barkety.{OutboundMessage,InboundMessage,CreateChat}

class BarketySpec extends Spec with ShouldMatchers with TestKit {

  describe("The Chat supervisor") {
    it("should boot with no problems") {
      val jid = JID("barketyTest@jabber.org")
      val chatsupRef = actorOf(new ChatSupervisor(jid, "123456")).start
      chatsupRef.stop
    }

    it("should create a chatter on request") {
      val jid = JID("barketyTest@jabber.org")
      val me  = JID("troutwine@jabber.org")
      val chatsup = actorOf(new ChatSupervisor(jid, "123456")).start
      (chatsup !! CreateChat(me)) should not { be === None }
      chatsup.stop
    }

    it("should send me a nice message") {
      val jid = JID("barketyTest@jabber.org")
      val me  = JID("troutwine@jabber.org")
      val chatsup = actorOf(new ChatSupervisor(jid, "123456")).start
      (chatsup !! CreateChat(me)) match {
        case Some(chatter:ActorRef) =>
          chatter ! OutboundMessage("Hi, you!")
        case None => fail()
      }
      Thread.sleep(1000)
      chatsup.stop
    }

    // BUG: Does not work as I expect
    // it("should allow me to send it a message") {
    //   val jid = JID("barketyTest@jabber.org")
    //   val me  = JID("troutwine@jabber.org")
    //   val chatsup = actorOf(new ChatSupervisor(jid, "123456")).start
    //   (chatsup !! CreateChat(me)) match {
    //     case Some(chatter:ActorRef) =>
    //       chatter ! RegisterParent(testActor)
    //       chatter ! OutboundMessage("Reply back with 'hi'")
    //       within (10 seconds) {
    //         expectMsg(InboundMessage("hi"))
    //       }
    //     case None => fail()
    //   }
    //   chatsup.stop
    // }
   }

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

    it("should provide sane equality") {
      val jid0:JID = JID("troutwine@jabber.org/")
      val jid1:JID = JID("troutwine@jabber.org/")
      jid1 should be === jid0
      val jid2:JID = JID("troutwine@jabber.org/helpful")
      jid1 should not { be === jid2 }
      val jid3:JID = JID("morbo@earth.org")
      jid1 should not { be === jid3 }
      jid0 match {
        case jid1 =>
        case _ => fail
      }
    }

    it("should enforce an arbitrary ordering") {
      val jid0:JID = JID("troutwine@jabber.org")
      val jid1:JID = JID("barkety@jabber.org")
      val jid2:JID = JID("oliver@jabber.org")
      jid1 should be >= jid0
      jid2 should be >= jid0
      jid1 should be >= jid2
    }
  }

}

