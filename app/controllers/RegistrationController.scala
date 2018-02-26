package controllers

import java.io.File
import java.nio.file.{Files, Paths}
import java.util.UUID
import javax.inject._

import models._
import play.api.mvc.MultipartFormData.FilePart
import utils.{FileUtils, ImageMagickUtils, PasswordUtils}
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class RegistrationController @Inject()(cc: ControllerComponents, userDAO: UserDAO, passwordUtils: PasswordUtils)
                                      (implicit ec: ExecutionContext)
  extends AbstractController(cc) with play.api.i18n.I18nSupport {

  def registrationPage() = Action { implicit request =>
    request.session.get("user").map { user =>
      Redirect(routes.HomeController.home())
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
          case Some(user) =>
            BadRequest(UserForm.userForm.withError("email",
              "The email address you have entered is already registered.").errorsAsJson)
          case None =>
            val salt = passwordUtils.generateRandomString()
            val id = userDAO.create(userData.name, userData.email, passwordUtils.encryptPassword(userData.newPassword, salt), salt)
            request.body.file("file").map {
              case FilePart(key, filename, contentType, file) =>
                val outputImage = routes.Assets.versioned("images/" + id +".jpg").toString
                if(file.length() > 0 && S3FileDetails.isImage(contentType.get)) {
                  val newFile = new File(outputImage)
                  newFile.createNewFile()
                  ImageMagickUtils.resizeImage(file.toPath.toString, 200, outputImage)
                  S3FileDetails.changeUserAvatar(UUID.randomUUID().toString, newFile)
                  Files.deleteIfExists(Paths.get(outputImage))
                }
            }
            Ok.withSession("user" -> id.get.toString)
        }
      }
    )
  }
}