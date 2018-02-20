package models

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json._
import utils.ValidationUtils

case class Password(newPassword: String, confirmPassword: String)

object Password {

  implicit object PasswordFormat extends Format[Password] {
    def writes(user: Password): JsValue = {
      val userSeq = Seq(
        "new_password" -> JsString(user.newPassword),
        "confirm_password" -> JsString(user.confirmPassword)
      )
      JsObject(userSeq)
    }

    def reads(json: JsValue): JsResult[Password] = {
      JsSuccess(Password("", ""))
    }
  }
}

object PasswordForm {
  val passwordForm = Form(
    mapping(
      "new_password" -> nonEmptyText.verifying(ValidationUtils.password),
      "confirm_password" -> nonEmptyText.verifying(ValidationUtils.password)
    )(Password.apply)(Password.unapply)
      verifying("Passwords don't match", password => password.newPassword == password.confirmPassword)
  )
}