package cn.playalot.play2.mailgun.templating

import play.api.http.MimeTypes
import play.twirl.api.{BufferedContent, Format}

class DualFormatEmail(buffer: String) extends BufferedContent[DualFormatEmail](scala.collection.immutable.Seq[DualFormatEmail](), buffer) {
  val contentType = MimeTypes.HTML
	lazy val toPlainText:String = ""
//  Scoup.parseHTML(buffer).textNodes.map { elem =>
//		elem.text
//	}.mkString
}

object DualFormat extends Format[DualFormatEmail] {
  def raw(text: String): DualFormatEmail = ??? // new DualFormatEmail(text) // FIXME need to actually do work 
  def escape(text: String): DualFormatEmail = ??? // new DualFormatEmail(text) // FIXME need to actually do work 

  def empty: DualFormatEmail = ???
  def fill(elements: scala.collection.immutable.Seq[DualFormatEmail]): DualFormatEmail = ???

}
