package controllers

import javax.inject._

import models.UserDAO
import play.api.mvc._
import play.api.libs.json.Json

@Singleton
class HomeController @Inject()(cc: ControllerComponents, userDAO: UserDAO)
  extends AbstractController(cc) with play.api.i18n.I18nSupport {

  def home() = Action { implicit request =>
    request.session.get("user").map { user =>
      Ok(views.html.userslist())
    }.getOrElse {
      Ok(views.html.login())
    }
  }

  def allUsers() = Action { implicit request =>
    request.session.get("user").map { user =>
      Ok(Json.toJson(userDAO.getAllUsers))
    }.getOrElse {
      Ok(views.html.login())
    }
  }
}
