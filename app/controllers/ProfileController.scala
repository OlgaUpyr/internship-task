package controllers

import models.{S3FileDetails, UserDAO, UserForm}
import play.api.libs.json.Json
import play.api.mvc._
import utils.{FileUtils, ImageMagickUtils, PasswordUtils}
import javax.inject._

import play.api.mvc.MultipartFormData.FilePart

import scala.concurrent.ExecutionContext


@Singleton
class ProfileController @Inject()(cc: ControllerComponents, userDAO: UserDAO)
                                 (implicit ec: ExecutionContext)
  extends AbstractController(cc) with play.api.i18n.I18nSupport {
  def editProfile() = Action { implicit request =>
    request.session.get("user").map { user =>
      Ok(views.html.editpage(user.toLong))
    }.getOrElse {
      Ok(views.html.login())
    }
  }

  def updateUserInfo(id: Long) = Action(parse.multipartFormData(FileUtils.handleFilePartAsFile)) { implicit request =>
    UserForm.editUserForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(formWithErrors.errorsAsJson)
      },
      userData => {
        request.body.file("file").map {
          case FilePart(key, filename, contentType, file) =>
            if(file.length() > 0) {
              val filePath = file.toPath
              ImageMagickUtils.resizeImage(filePath.toString, 200)
              S3FileDetails.changeUserAvatar(id, filePath.toFile)
            }
        }
        if(userData.currentPassword.get == "" || userData.newPassword == "" || userData.confirmPassword == ""){
          userDAO.editProfile(id, userData.name, userData.email)
        } else {
          var user = userDAO.findById(id).get
          if(PasswordUtils.passwordsMatch(userData.currentPassword.get, user)){
            userDAO.editProfileWithPassword(id, userData.name, userData.email,
              PasswordUtils.encryptPassword(userData.newPassword, user.salt.get))
          } else {
            BadRequest(UserForm.editUserForm.withError("current_password",
              "The password you have entered does not match your current one.").errorsAsJson)
          }
        }
        Ok(Json.toJson(userData))
      }
    )
  }

  def profileInfo() = Action { implicit request =>
    request.session.get("user").map { user =>
      Ok(Json.toJson(userDAO.findById(user.toLong)))
    }.getOrElse {
      Ok(views.html.login())
    }
  }
}