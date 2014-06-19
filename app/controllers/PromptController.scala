package controllers

import play.api.mvc._
import play.api.db.slick._
import play.api.db.slick.Config.driver.simple._
import models.{Prompt, prompts}
import play.api.libs.functional.syntax._
import play.api.libs.json._
import java.sql.Timestamp
import java.text.SimpleDateFormat
import play.api.data.validation.ValidationError

/**
 * Created by jiarui.yan on 6/17/14.
 */

object PromptController extends Controller {

  implicit object DateFormat extends Format[java.sql.Timestamp] {

    override def reads(json: JsValue): JsResult[Timestamp] = json match {
      case JsString(s) => parseDate(s) match {
        case Some(d) => JsSuccess(new Timestamp(d.getTime))
        case None => JsError(Seq(JsPath() -> Seq(ValidationError("error.expected.date.isoformat", "yyyy-MM-ddTHH:mm:ss"))))
      }
    }

    override def writes(o: Timestamp): JsValue = JsString(formatDate(o))

    private def parseDate(input: String): Option[java.util.Date] = {
      val df = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
      df.setLenient(false)
      try { Some(df.parse(input)) } catch {
        case _: java.text.ParseException => None
      }
    }

    private def formatDate(ts: Timestamp): String = {
      val df = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
      df.setLenient(false)
      df.format(ts)
    }
  }

  implicit val PromptReads: Reads[Prompt] = (
    (JsPath \ "id").read[Long] and
      (JsPath \ "text").read[String] and
      (JsPath \ "imagePath").read[String] and
      (JsPath \ "startDateTime").read[Timestamp] and
      (JsPath \ "endDateTime").read[Timestamp] and
      (JsPath \ "status").read[Boolean]
    )(Prompt.apply _)

  implicit val PromptWrites: Writes[Prompt] = (
    (JsPath \ "id").write[Long] and
      (JsPath \ "text").write[String] and
      (JsPath \ "imagePath").write[String] and
      (JsPath \ "startDateTime").write[Timestamp] and
      (JsPath \ "endDateTime").write[Timestamp] and
      (JsPath \ "status").write[Boolean]
    )(unlift(Prompt.unapply))

  def random = SimpleFunction[Long]("random").apply(Seq.empty);

  def allPrompts = DBAction { implicit rs =>

    val now = new java.sql.Timestamp(new java.util.Date().getTime)

    val validPrompts = (for {
      p <- prompts
      if p.endDateTime > now
      if p.startDateTime <= now
      if p.active
    } yield p).list

    //Ok(views.html.index("Test.")(validPrompts.list))
    Ok(Json.toJson(validPrompts))

  }

  def anyPrompt = DBAction { implicit rs =>

    val now = new java.sql.Timestamp(new java.util.Date().getTime)

    val anyPrompt = (for {
      p <- prompts
      if p.endDateTime > now
      if p.startDateTime <= now
      if p.active
    } yield p).sortBy(_ => random).take(1)


    //Ok(views.html.index("Your new application is ready.")(anyPrompt.list))
    Ok(Json.toJson(anyPrompt.list))

  }

  def prompt(id: Long) = DBAction { implicit rs =>

    val prompt = prompts.filter(p => p.id === id)

    Ok(Json.toJson(prompt.list))
  }

  def newPrompt = DBAction(BodyParsers.parse.json) { implicit rs =>

    val request = rs.request

    val prompt = request.body.validate[Prompt]

    prompt.fold(
      errors => {
        BadRequest(Json.obj("status" ->"KO", "message" -> JsError.toFlatJson(errors)))
      },
      p => {
        prompts += p
        Ok(Json.obj("status" ->"OK", "message" -> ("Prompt '"+ p.text +"' is saved.") ))
        //Redirect(routes.PromptController.allPrompts)
      }
    )
  }

}
