package us.troutwine.barkety

import akka.actor._
import akka.actor.Actor._
import akka.config.Supervision.AllForOneStrategy
import scala.util.matching.Regex
import scala.collection.mutable.Builder
import akka.event.EventHandler

/**
 * Defines the default [[us.troutwine.barkety.Bot.CommandParser]] implicit, 
 * where bot commands are prefixed with '!', e.g., "!doStuff arg1 arg2 ...".
 */
object BotDefaults {
  import Bot._
  
  implicit val commandParser = new CommandParser() {
    val pat = """\!(\w+)\s*(.*)""".r
    def parse(msg: String) = msg match {
      case pat(name, args) => Some(Command(name, Option(args).map(_.split(" ")).getOrElse(Array())))
      case _ => None
    }
  }
}

object Bot {
  sealed trait BotMessage
  case class Command(name: String, args: Array[String]) extends BotMessage
  
  type RoomMessage = String
  type Trigger = PartialFunction[(ActorRef, RoomMessage), Unit]
  case class CommandDef(name: String, help: String, trigger: Trigger)

  trait CommandParser {
    def parse(msg: String): Option[Command]
  }
  
  object ParsedCommand {
    def unapply(msg: RoomMessage)(implicit commandParser: CommandParser): Option[Command] = { 
      commandParser.parse(msg)
    }
  }
  
  class RoomBotBuilder(room: ActorRef)(implicit commandParser: CommandParser) {
    val commands = Map.newBuilder[String, CommandDef]
    
    def result(): ActorRef = { 
      actorOf(new RoomBot(room, commandsWithHelp()))
    }
    
    protected def commandsWithHelp(): Seq[CommandDef] = {
      val c = commands.result().values.toSeq
      val detachedHelp = newCommand("help", "Show this help message") { case _ => () } 
      val help = newCommand(detachedHelp.name, detachedHelp.help) { (room: ActorRef, args: Array[String]) =>
        room ! (c :+ detachedHelp).sortBy(_.name).map(c => "%s: %s".format(c.name, c.help)).mkString("\n")
      }
      c :+ help
    }

    def +=(name: String, help: String, action: (ActorRef, Array[String]) => Unit) : RoomBotBuilder = {
      commands += name -> (newCommand(name, help)(action))
      this
    }
    
    def newCommand(name: String, help: String)(action: (ActorRef, Array[String]) => Unit): CommandDef = 
      CommandDef(name, help, {
        case (room, ParsedCommand(command)) if command.name == name => action(room, command.args)
      })
  }
  
  def newRoomBot(room: ActorRef)(implicit commandParser: CommandParser): RoomBotBuilder = new RoomBotBuilder(room)
}

class RoomBot(room: ActorRef, commands: Seq[Bot.CommandDef]) extends Actor {
  import Bot._
  
  // Please accept from me this unpretentious bouquet of very early-blooming parentheses:
  val noOp: Trigger = { case (room, msg) => EventHandler.debug(this, "room %s, noOp trying to match command %s".format(room, msg)) } 
  
  override def preStart() = room ! RegisterParent(self)
  
  def receive = {
    case InboundMessage(msg) => 
      EventHandler.debug(this, commands)
      ((commands.map(_.trigger) :+ noOp).reduce(_ orElse _))(room, msg)
  }
}