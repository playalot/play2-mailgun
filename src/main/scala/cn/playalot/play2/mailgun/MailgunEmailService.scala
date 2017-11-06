package cn.playalot.play2.mailgun

import javax.inject.Inject

import akka.stream.scaladsl.{ FileIO, Source }
import akka.util.ByteString
import cn.playalot.play2.mailgun.MailgunEmailService.AttachmentPartType
import play.api.{ Configuration, Logger }
import play.api.http.Status
import play.api.libs.ws.{ WSAuthScheme, WSClient, WSRequest, WSResponse }
import play.api.mvc.MultipartFormData
import play.api.mvc.MultipartFormData.{ DataPart, FilePart, Part }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/** For static-style usage: */
object MailgunEmailService {
  type AttachmentPartType = Source[ByteString, Any]
  type PostData = Source[MultipartFormData.Part[AttachmentPartType], _]
}

class MailgunEmailService @Inject() (wsClient: WSClient, configuration: Configuration) extends MailgunResponseJson {

  lazy val mailgunApiKey: String = configuration.get[String]("mailgun.api.key")
  lazy val defaultSender: Option[String] = configuration.getOptional[String]("mailgun.default.sender")
  lazy val mailgunUrl: String = configuration.get[String]("mailgun.api.url")
  lazy val ws: WSRequest = wsClient.url(mailgunUrl)

  /** Sends the message via Mailgun's API, respecting any options provided */
  def send(message: EssentialEmailMessage, options: Set[MailgunOption] = Set()): Future[MailgunResponse] = {
    if (defaultSender.isEmpty && message.from.isEmpty) {
      Future.failed(new IllegalStateException("From: field is None and no default sender configured"))
    } else {
      val sender = message.from.getOrElse(defaultSender.get)

      ws.withAuth("api", mailgunApiKey, WSAuthScheme.BASIC)
        .post(buildMultipartRequest(sender, message, options))
        .flatMap(handleMailgunResponse)
    }
  }

  private def buildMultipartRequest(sender: String, message: EssentialEmailMessage, options: Set[MailgunOption]): MailgunEmailService.PostData = {
    val requiredParts: List[Part[AttachmentPartType]] = List(
      DataPart("from", sender),
      DataPart("to", message.to),
      DataPart("subject", message.subject),
      DataPart("text", message.text),
      DataPart("html", message.html.toString())
    )

    val optionalParts: List[Part[AttachmentPartType]] = List(
      message.cc.map(DataPart("cc", _)),
      message.bcc.map(DataPart("bcc", _))
    ).flatten ++ message.computedHeaders.map(hdr => DataPart(s"h:${hdr._1}", hdr._2))

    val attachments = message.attachments.map(buildAttachment)

    Source(addOptions(requiredParts ++ optionalParts ++ attachments, options))
  }

  private def buildAttachment(attachment: MailgunAttachment): FilePart[AttachmentPartType] = {
    val theFile = attachment.file
    FilePart[Source[ByteString, Any]](
      "attachment",
      attachment.fileName,
      attachment.contentType,
      FileIO.fromFile(theFile)
    )
  }

  private def addOptions(basicParts: List[Part[AttachmentPartType]], options: Set[MailgunOption]): List[Part[AttachmentPartType]] = {
    basicParts ++ options.map { o =>
      Logger.debug(s"Adding option $o: ${o.renderAsApiParameter}")
      o.renderAsApiParameter
    }
  }

  /**
   * As per https://documentation.mailgun.com/api-intro.html#errors
   */
  private def handleMailgunResponse(response: WSResponse): Future[MailgunResponse] = {
    if (response.status == Status.OK) {
      Future.successful(response.json.as[MailgunResponse])
    } else {
      Future.failed(
        response.status match {
          case Status.UNAUTHORIZED => new MailgunAuthenticationException((response.json \ "message").as[String])
          case _                   => new MailgunSendingException((response.json \ "message").as[String])
        }
      )
    }

  }
}
