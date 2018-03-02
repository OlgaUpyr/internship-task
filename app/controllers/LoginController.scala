package controllers

import javax.inject._

import models._
import utils.PasswordUtils
import play.api.mvc._

@Singleton
class LoginController @Inject()(cc: ControllerComponents, userDAO: UserDAO, passwordUtils: PasswordUtils)
  extends AbstractController(cc) with play.api.i18n.I18nSupport {

  def ping = Action {
    Ok("PONG")
  }

  def loginPage() = Action { implicit request =>
    request.session.get("user").map { user =>
      Redirect(routes.HomeController.home())
    }.getOrElse {
      Ok(views.html.login())
    }
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
            if(userDAO.checkRole(user.id.get) == userData.role){
              if(passwordUtils.passwordsMatch(userData.password, user.salt.get, user.newPassword))
                Ok.withSession("user" -> user.id.get.toString)
              else
                BadRequest(LoginForm.loginForm.withError("password", "Password is invalid.").errorsAsJson)
            } else {
              BadRequest(LoginForm.loginForm.withError("role", "Wrong user role.").errorsAsJson)
            }
        }
      }
    )
  }

  def logout() = Action { implicit request =>
    request.session.get("user").map { user =>
      Redirect(routes.LoginController.loginPage()).withNewSession
    }.getOrElse {
      Ok(views.html.login())
    }
  }
}