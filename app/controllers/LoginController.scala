package controllers

import javax.inject._

import models._
import utils.PasswordUtils
import play.api.mvc._

@Singleton
class LoginController @Inject()(cc: ControllerComponents, userDAO: UserDAO)
  extends AbstractController(cc) with play.api.i18n.I18nSupport {

  def loginPage() = Action {
    Redirect(routes.HomeController.home())
  }

  def login() = Action { implicit request =>
    LoginForm.loginForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(formWithErrors.errorsAsJson)
      },
      userData => {
        userDAO.findByEmail(userData.email) match {
          case None =>
            BadRequest(LoginForm.loginForm.withError("email", "Wrong email address.").errorsAsJson)
          case Some(user) =>
            if(PasswordUtils.passwordsMatch(userData.password, user))
              Ok.withSession("user" -> user.id.get.toString)
            else
              BadRequest(LoginForm.loginForm.withError("password", "Password is invalid.").errorsAsJson)
        }
      }
    )
  }

  def logout() = Action { implicit request =>
    Redirect(routes.LoginController.loginPage()).withNewSession
  }
}