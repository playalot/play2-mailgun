package cn.playalot.play2.mailgun

case class MailgunResponse(message: String, id: String) {
  lazy val status: MailgunSentMessageStatus = MessageQueued
}
