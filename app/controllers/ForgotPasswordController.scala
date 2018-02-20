package controllers

import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject._

import models.{EmailForm, PasswordForm, UserDAO}
import org.joda.time.DateTime
import play.api.mvc._
import play.api.libs.mailer._
import utils.PasswordUtils


@Singleton
class ForgotPasswordController @Inject()(cc: ControllerComponents, userDAO: UserDAO, mailerClient: MailerClient)
  extends AbstractController(cc) with play.api.i18n.I18nSupport {

  private val EMAIL_BODY_TEXT = "You has requested to reset the password for your account.\n\n" +
    "If you did not perform this request, you can safely ignore this email.\n" +
    "Otherwise, click the link below to complete the process.\n%s"

  def checkEmailPage() = Action {
    Ok(views.html.checkemailpage())
  }

  def sendEmail() = Action { implicit request =>
    EmailForm.emailForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(formWithErrors.errorsAsJson)
      },
      userData => {
        userDAO.findByEmail(userData.email) match {
          case None =>
            BadRequest(EmailForm.emailForm.withError("email",
              "There is no account with this email address.").errorsAsJson)
          case Some(user) =>
            val controlKey = UUID.randomUUID().toString
            userDAO.setControlKey(userData.email, controlKey,
              new DateTime(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10)))

            val email = Email(
              "Reset password instruction",
              "olga.upyr1@gmail.com",
              Seq(userData.email),
              bodyText = Some(String.format(EMAIL_BODY_TEXT,
                routes.ForgotPasswordController.forgotPasswordPage(controlKey.toString).absoluteURL()))
            )
            mailerClient.send(email)
            Ok
        }
      }
    )
  }

  def forgotPasswordPage(token: String) = Action { implicit request =>
    userDAO.findByControlKey(token, new DateTime(System.currentTimeMillis())) match {
      case Some(user) =>
        Ok(views.html.forgotpasswordpage(token))
      case None =>
        Ok(views.html.home())
    }
  }

  def changePassword(token: String) = Action { implicit request =>
    PasswordForm.passwordForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(formWithErrors.errorsAsJson)
      },
      userData => {
        val currentUser = userDAO.findByControlKey(token, new DateTime(System.currentTimeMillis())).get
        userDAO.changePassword(currentUser.id.get, PasswordUtils.encryptPassword(userData.newPassword, currentUser.salt.get))
        Ok
      }
    )
  }
}
