package models

import play.api.data.Form
import play.api.data.Forms._
import utils.ValidationUtils

case class Mail(email: String)

object EmailForm {
  val emailForm = Form(
    mapping(
      "email" -> email.verifying(ValidationUtils.email),
    )(Mail.apply)(Mail.unapply)
  )
}
