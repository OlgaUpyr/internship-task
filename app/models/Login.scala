package models

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json._
import utils.ValidationUtils

case class Login(email: String, password: String, role: String)

object Login {

  implicit object LoginFormat extends Format[Login] {
    def writes(user: Login): JsValue = {
      val userSeq = Seq(
        "email" -> JsString(user.email),
        "password" -> JsString(user.password),
        "role" -> JsString(user.role)
      )
      JsObject(userSeq)
    }

    def reads(json: JsValue): JsResult[Login] = {
      JsSuccess(Login("", "", ""))
    }
  }
}

object LoginForm {
  val loginForm = Form(
    mapping(
      "email" -> email.verifying(ValidationUtils.email),
      "password" -> nonEmptyText.verifying(ValidationUtils.password),
      "role" -> nonEmptyText
    )(Login.apply)(Login.unapply)
  )
}


