package us.troutwine.barkety

import akka.actor.{Actor,ActorRef}
import akka.event.{EventHandler => log}
import akka.config.Supervision.AllForOneStrategy

import scala.collection.mutable

import org.jivesoftware.smack.{XMPPConnection,ChatManagerListener,Chat}
import org.jivesoftware.smack.{MessageListener,Roster,ConnectionConfiguration}
import org.jivesoftware.smack.packet.{Message,Presence}

private sealed abstract class InternalMessage
private case class RemoteChatCreated(jid:JID,chat:Chat) extends InternalMessage
private case class ReceivedMessage(msg:String) extends InternalMessage

sealed abstract class Memo
case class CreateChat(jid:JID) extends Memo
case class RegisterParent(ref:ActorRef) extends Memo
case class InboundMessage(msg:String) extends Memo
case class OutboundMessage(msg:String) extends Memo

private class ChatListener(parent:ActorRef) extends ChatManagerListener {
  override def chatCreated(chat:Chat, createdLocally:Boolean) = {
    val jid:JID = JID(chat.getParticipant)
    if (createdLocally)
      log.info(this,"A local chat with %s was created.".format(jid))
    else {
      log.info(this,"%s has begun to chat with us.".format(jid))
      parent ! RemoteChatCreated(jid, chat)
    }
  }
}

private class MsgListener(parent:ActorRef) extends MessageListener {
  override def processMessage(chat:Chat,msg:Message) = {
    if (msg.getBody != null)
      parent ! ReceivedMessage(msg.getBody)
  }
}

private class MsgLogger extends MessageListener {
  override def processMessage(chat:Chat,msg:Message) = {
    log.info(this, "INBOUND %s --> %s : %s".format(chat.getParticipant,
                                                   chat.getThreadID,
                                                   msg.getBody))
  }
}

private class Chatter(chat:Chat, roster:Roster) extends Actor {
  chat.addMessageListener(new MsgListener(self))
  var parent:Option[ActorRef] = None

  override def receive = {
    case RegisterParent(ref) =>
      parent = Some(ref)
    case OutboundMessage(msg) =>
      if ( roster.contains(chat.getParticipant) )
        chat.sendMessage(msg)
    case msg:String =>
      chat.sendMessage(msg)
    case msg:ReceivedMessage =>
      parent match {
        case Some(ref) => ref ! InboundMessage(msg.msg)
        case None =>
      }
  }
}

class ChatSupervisor(jid:JID, password:String,
                     domain:Option[String] = None,
                     port:Option[Int] = None) extends Actor
{
  self.faultHandler = AllForOneStrategy(List(classOf[Throwable]), 5, 5000)
  self.id = "chatsupervisor:%s".format(jid)

  private val conf = new ConnectionConfiguration(domain.getOrElse(jid.domain),
                          port.getOrElse(5222), jid.domain)
  private val conn = new XMPPConnection(conf)
  conn.connect()
  domain match {
    case Some("talk.google.com") => conn.login(jid, password)
    case _ => conn.login(jid.username, password)
  }
  private val roster:Roster = conn.getRoster()
  roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all)
  conn.sendPacket( new Presence(Presence.Type.available) )

  private val chats:mutable.Map[JID,Chat] = new mutable.HashMap
  private val msglog:MsgLogger = new MsgLogger

  override def receive = {
    case CreateChat(partnerJID) => {
      val chat = conn.getChatManager().createChat(partnerJID, msglog)
      if ( !roster.contains(partnerJID) )
        roster.createEntry(partnerJID, partnerJID, null)
      val chatter = Actor.actorOf(new Chatter(chat, roster)).start
      self.link(chatter)
      self.reply_?(chatter)
    }
    case RemoteChatCreated(partnerJID,chat) =>
      chats.put(partnerJID,chat)
    case _ => throw new RuntimeException("I do nothing!")
  }

  override def postStop = {
    conn.disconnect()
  }
}
