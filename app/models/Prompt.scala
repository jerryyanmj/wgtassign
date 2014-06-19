package models

import play.api.db.slick.Config.driver.simple._
import java.sql.Timestamp

case class Prompt(id: Long, text: String, imagePath: String, startDateTime: Timestamp, endDateTime: Timestamp, active: Boolean)

class PromptTable(tag: Tag) extends Table[Prompt](tag, "prompt") {

  def id = column[Long]("prompt_id", O.PrimaryKey, O.AutoInc)
  def text = column[String]("prompt_text")
  def imagePath = column[String]("image_path")
  def startDateTime = column[Timestamp]("start_date_time")
  def endDateTime = column[Timestamp]("end_date_time")
  def active = column[Boolean]("active")
  def * = (id, text, imagePath, startDateTime, endDateTime, active) <> (Prompt.tupled, Prompt.unapply)

}

object prompts extends TableQuery(tag => new PromptTable(tag))


