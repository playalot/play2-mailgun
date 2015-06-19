package com.themillhousegroup.play2.mailgun

import play.api.libs.concurrent.Execution.Implicits._
import play.api.{ Logger, Play }
import scala.concurrent.Future
import org.apache.commons.lang3.StringUtils
import play.api.http._
import play.api.libs.ws._
import play.api.Play.current
import com.ning.http.client.FluentCaseInsensitiveStringsMap
import com.ning.http.multipart._
import java.io.ByteArrayOutputStream

import com.ning.http.multipart.{ FilePart, MultipartRequestEntity, Part }

object EmailService extends EmailService(
  Play.current.configuration.getString("mailgun.api.key").get,
  Play.current.configuration.getString("mailgun.default.sender"))(
  WS.url(Play.current.configuration.getString("mailgun.api.url").get))

class EmailService(val mailgunApiKey: String, val defaultSender: Option[String])(val ws: WSRequestHolder) {

  //client.addFilter(new HTTPBasicAuthFilter("api", mailgunApiKey))
  //val webResource = client.resource(mailgunApiUrl)

  def send(message: EssentialEmailMessage): Future[Option[String]] = {

    if (defaultSender.isEmpty && message.from.isEmpty) {
      Future.failed[Option[String]](new IllegalStateException("From: field is None and no default sender configured"))
    } else {
      val sender = message.from.getOrElse(defaultSender.get)

      //    val logo = Play.getExistingFile("/public/images/logo.png").get
      //    form.bodyPart(new FileDataBodyPart("inline", logo, MediaType.APPLICATION_OCTET_STREAM_TYPE))

      // Use the Ning AsyncHttpClient multipart class to get the bytes
      val parts = Array[Part](
        new StringPart("from", sender),
        new StringPart("to", message.to),
        new StringPart("subject", message.subject),
        new StringPart("text", message.text),
        new StringPart("html", message.html.toString())
      )
      //      new FilePart("attachment", file)

      val mpre = new MultipartRequestEntity(parts, new FluentCaseInsensitiveStringsMap)
      val baos = new ByteArrayOutputStream
      mpre.writeRequest(baos)
      val bytes = baos.toByteArray
      val contentType = mpre.getContentType

      ws.post(bytes)(Writeable.wBytes, ContentTypeOf(Some(contentType))).map { response =>
        Option(StringUtils.trimToNull(message.to))

      }
    }
  }
}
