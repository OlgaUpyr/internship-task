package controllers

import java.io.File
import java.nio.file.{Files, Paths}
import java.util.UUID

import models.{ProductDAO, S3FileDetails, UserDAO, UserForm}
import play.api.libs.json.Json
import play.api.mvc._
import utils.{FileUtils, ImageMagickUtils, PasswordUtils}
import javax.inject._

import play.api.mvc.MultipartFormData.FilePart

import scala.concurrent.ExecutionContext


@Singleton
class ProfileController @Inject()(cc: ControllerComponents, userDAO: UserDAO, passwordUtils: PasswordUtils, productDAO: ProductDAO)
                                 (implicit ec: ExecutionContext)
  extends AbstractController(cc) with play.api.i18n.I18nSupport {
  def editProfile() = Action { implicit request =>
    request.session.get("user").map { user =>
      Ok(views.html.editpage(user.toLong, userDAO))
    }.getOrElse {
      Redirect(routes.LoginController.loginPage())
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
            if(file.length() > 0 && S3FileDetails.isImage(contentType.get)) {
              val avatarUrl = UUID.randomUUID().toString
              val outputImage = "C:/internship-task/"+ avatarUrl +".jpg"
              val newFile = new File(outputImage)
              newFile.createNewFile()
              ImageMagickUtils.resizeImage(file.toPath.toString, 200, outputImage)
              S3FileDetails.changeUserAvatar(avatarUrl, newFile)
              userDAO.changeAvatarUrl(id, avatarUrl)
              Files.deleteIfExists(Paths.get(outputImage))
            }
        }
        val currentUser = userDAO.findById(id).get
        if(userDAO.isEmailExist(currentUser.email, userData.email)) {
          BadRequest(UserForm.editUserForm.withError("email",
            "The email address you have entered is already registered.").errorsAsJson)
        }
        else {
          if(userData.currentPassword.get == "" && userData.newPassword == "" && userData.confirmPassword == ""){
            userDAO.editProfile(id, userData.name, userData.email)
            Ok(Json.toJson(userData))
          } else if (userData.currentPassword.get != "" && userData.newPassword != "" && userData.confirmPassword != "") {
            var user = userDAO.findById(id).get
            if(passwordUtils.passwordsMatch(userData.currentPassword.get, user.salt.get, user.newPassword)){
              userDAO.editProfileWithPassword(id, userData.name, userData.email,
                passwordUtils.encryptPassword(userData.newPassword, user.salt.get))
              Ok(Json.toJson(userData))
            } else {
              BadRequest(UserForm.editUserForm.withError("current_password",
                "The password you have entered does not match your current one.").errorsAsJson)
            }
          } else {
            BadRequest(UserForm.editUserForm.withError("", "Missing fields.").errorsAsJson)
          }
        }
      }
    )
  }

  def getProfileInfo() = Action { implicit request =>
    request.session.get("user").map { user =>
      Ok(views.html.profilepage(userDAO, userDAO.findById(user.toLong).get))
    }.getOrElse{
      Redirect(routes.HomeController.home())
    }
  }

  def profileInfo() = Action { implicit request =>
    request.session.get("user").map { user =>
      Ok(Json.toJson(userDAO.findById(user.toLong)))
    }.getOrElse {
      Unauthorized
    }
  }
}