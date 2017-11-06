package cn.playalot.play2.mailgun

import play.api.libs.json.Json

trait MailgunResponseJson {
  implicit val responseReads = Json.reads[MailgunResponse]
}
