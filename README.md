Barkety - An Akka compatible XMPP client library.
=================================================

While the [Apache Camel](http://camel.apache.org) project has an xmpp
component and though [Jive Software's
Smack](http://www.igniterealtime.org/projects/smack/) is ubiquitous neither
fulfil my desires:

  * Convenient roster management,
  * supervision heirarchy ready,
  * and actor-level isolation of state management.

Akka's bindings to camel-xmpp provide the last two and plain Smack allows only
the first point.

Installation
------------

Kindly include this in your project definition:

```
val troutwineRepo = "troutwine.us repository" at "http://maven.troutwine.us/"
val phone = "us.troutwine" %% "Barkety" % "0.1.0"
```

Usage
-----

The tippy-top of the supervision heirarchy is ChatSupervisor. It understands
`CreateChat(partner:JID)` messages and will reply back with an ActorRef to a
Chatter actor. You must send this actor `RegisterParent(ref:ActorRef)` in
order to route inbound messages to another
actor. `OutboundMessage(msg:String)` will send a message to the partner
connected by the Chatter.

By default, all chats are preceded by roster subscription invitations.

```
import us.troutwine.barkety._
import akka.actor.Actor.actorOf
import akka.actor.{Actor,ActorRef}

class Acty(child:ActorRef) extends Actor {
  child ! RegisterParent(self)

  def receive = {
    case InboundMessage(msg:String) => println(msg)
  }
}

object Main extends Application {
  val jid = JID("barketyTest@jabber.org")
  val chatsup = actorOf(new ChatSupervisor(jid, "123456")).start
  (chatsup !! CreateChat(JID("troutwine@jabber.org"))) match {
    case Some(chatter) =>
      actorOf(new Acty(chatter)).start
      chatter ! OutboundMessage("Hi, you!")
    case None =>
  }
}
```