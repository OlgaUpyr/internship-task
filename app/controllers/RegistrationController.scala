package controllers

import javax.inject._

import models._
import play.api.mvc.MultipartFormData.FilePart
import utils.{FileUtils, ImageMagickUtils, PasswordUtils}
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class RegistrationController @Inject()(cc: ControllerComponents, userDAO: UserDAO)
                                      (implicit ec: ExecutionContext)
  extends AbstractController(cc) with play.api.i18n.I18nSupport {

  def registrationPage() = Action { implicit request =>
    request.session.get("user").map { user =>
      Ok(views.html.userslist())
    }.getOrElse {
      Ok(views.html.registration())
    }
  }

  def register() = Action(parse.multipartFormData(FileUtils.handleFilePartAsFile)) { implicit request =>
    UserForm.userForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(formWithErrors.errorsAsJson)
      },
      userData => {
        userDAO.findByEmail(userData.email) match {
          case None =>
            val salt = PasswordUtils.generateRandomString(128)
            val id = userDAO.create(userData.name, userData.email,
              PasswordUtils.encryptPassword(userData.newPassword, salt), salt)
            request.body.file("file").map {
              case FilePart(key, filename, contentType, file) =>
                Console.println("ooooooooooooooooouuuuuuuuuuuuuu")
                if(file.length() > 0) {
                  //ImageMagickUtils.resizeImage(filename, 200)
                  S3FileDetails.changeUserAvatar(id.get, file)
                }
            }
            Ok.withSession("user" -> id.get.toString)
          case Some(user) =>
            BadRequest(UserForm.userForm.withError("email",
              "The email address you have entered is already registered.").errorsAsJson)
        }
      }
    )
  }
}