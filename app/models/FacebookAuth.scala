package models

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, text}
import play.api.libs.json._

case class FacebookAuth(userId: String, name: String, email: String, avatarUrl: String, role: String)

object FacebookAuth {

  implicit object FacebookFormat extends Format[FacebookAuth] {
    def writes(facebook: FacebookAuth): JsValue = {
      val facebookSeq = Seq(
        "userId" -> JsString(facebook.userId),
        "name" -> JsString(facebook.name),
        "email" -> JsString(facebook.email),
        "avatarUrl" -> JsString(facebook.avatarUrl),
        "role" -> JsString(facebook.role)
      )
      JsObject(facebookSeq)
    }

    def reads(json: JsValue): JsResult[FacebookAuth] = {
      JsSuccess(FacebookAuth("", "", "", "", ""))
    }
  }
}

object FacebookAuthForm {
  val fbAuthForm = Form(
    mapping(
      "userId" -> nonEmptyText,
      "name" -> nonEmptyText,
      "email" -> nonEmptyText,
      "avatarUrl" -> nonEmptyText,
      "role" -> nonEmptyText
    )(FacebookAuth.apply)(FacebookAuth.unapply)
  )
}
