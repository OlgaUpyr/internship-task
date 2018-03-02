package models

import org.joda.time.DateTime
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json._
import utils.ValidationUtils

case class User(id: Option[Long], fbId: Option[String], name: String, email: String, currentPassword: Option[String], newPassword: String,
                confirmPassword: String, salt: Option[String], controlKey: Option[String], expirationTime: Option[DateTime],
                avatarUrl: Option[String], role: Option[String])

object User {

  implicit object UserFormat extends Format[User] {
    def writes(user: User): JsValue = {
      val userSeq = Seq(
        "name" -> JsString(user.name),
        "email" -> JsString(user.email),
        "new_password" -> JsString(user.newPassword),
        "confirm_password" -> JsString(user.confirmPassword)
      )
      JsObject(userSeq)
    }

    def reads(json: JsValue): JsResult[User] = {
      JsSuccess(User(null, null, "", "", null, "", "", null, null, null, null, null))
    }
  }
}

object UserForm {
  val userForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "email" -> nonEmptyText.verifying(ValidationUtils.email),
      "new_password" -> nonEmptyText.verifying(ValidationUtils.password),
      "confirm_password" -> nonEmptyText.verifying(ValidationUtils.password),
      "role" -> text
    )((a, b, c, d, e) => User(None, None, a, b, None, c, d, None, None, None, None, Some(e)))
    (u => Some(u.name, u.email, u.newPassword, u.confirmPassword, u.role.get))
      verifying("Passwords don't match", password => password.newPassword == password.confirmPassword)
  )

  val editUserForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "email" -> nonEmptyText.verifying(ValidationUtils.email),
      "current_password" -> text.verifying(ValidationUtils.password),
      "new_password" -> text.verifying(ValidationUtils.password),
      "confirm_password" -> text.verifying(ValidationUtils.password)
    )((a, b, c, d, e) => User(None, None, a, b, Some(c), d, e, None, None, None, None, None))
    (u => Some(u.name, u.email,u.currentPassword.get, u.newPassword, u.confirmPassword))
      verifying("Passwords don't match", password => password.newPassword == password.confirmPassword)
  )
}