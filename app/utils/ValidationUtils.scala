package utils

import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

object ValidationUtils {

  val email: Constraint[String] = Constraint("constraints.email")({
    var email_regex = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?"
    plainText =>
      val errors = plainText match {
        case wrongEmail if !wrongEmail.matches(email_regex) => Seq(ValidationError("Wrong email address."))
        case _ => Nil
      }
      if (errors.isEmpty) {
        Valid
      } else {
        Invalid(errors)
      }
  })


  val password: Constraint[String] = Constraint("constraints.password")({
    plainText =>
      val errors = plainText match {
        case shortPassword if shortPassword.length > 0 && shortPassword.length < 8 =>
          Seq(ValidationError("Password must be at least 8 characters in length."))
        case _ => Nil
      }
      if (errors.isEmpty) {
        Valid
      } else {
        Invalid(errors)
      }
  })
}
