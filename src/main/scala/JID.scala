package us.troutwine.barkety

import scala.util.matching.Regex

object JID {
  private val jidRe = new Regex("""(?i)(\w+)@([^/]+)/?+(.+)?+""")

  def apply(parts: Seq[String]): String =
    parts.size match {
      case 2 => """%s@%s""".format(parts:_*)
      case 3 => """%s@%s/%s""".format(parts:_*)
    }

  def unapplySeq(jid:String): Option[Seq[String]] =
    jid match {
      case jidRe(username,domain,resource) =>
        if (resource == null)
          Some( Seq(username,domain) )
        else
          Some( Seq(username,domain,resource) )
      case _ => None
    }
}

