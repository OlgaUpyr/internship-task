package controllers

import java.io.File
import java.nio.file.{Files, Paths}
import java.util.UUID
import javax.inject._

import models.{FacebookAuthForm, LoginForm, S3FileDetails, UserDAO}
import play.api.mvc._
import utils.ImageMagickUtils

@Singleton
class FacebookAuthController @Inject()(cc: ControllerComponents, userDAO: UserDAO)
  extends AbstractController(cc) with play.api.i18n.I18nSupport {
  def facebookAuth() = Action { implicit request =>
    FacebookAuthForm.fbAuthForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(formWithErrors.errorsAsJson)
      },
      fbData => {
        userDAO.findByEmail(fbData.email) match {
          case None =>
            userDAO.findByFBId(fbData.userId) match {
              case None =>
                val newUser = userDAO.createFBUser(fbData.userId, fbData.name).get
                if (fbData.email != "none") {
                  userDAO.setEmail(newUser, fbData.email)
                }
                if (fbData.avatarUrl != "none") {
                  val avatarUrl = UUID.randomUUID().toString
                  val outputImage = "C:/internship-task/" + avatarUrl + ".jpg"
                  val newFile = new File(outputImage)
                  newFile.createNewFile()
                  ImageMagickUtils.resizeImage(fbData.avatarUrl, 200, outputImage)
                  S3FileDetails.changeUserAvatar(avatarUrl, newFile)
                  userDAO.changeAvatarUrl(newUser, avatarUrl)
                  Files.deleteIfExists(Paths.get(outputImage))
                }
                Ok.withSession("user" -> newUser.toString)
              case Some(user) =>
                Ok.withSession("user" -> user.id.get.toString)
            }
          case Some(user) =>
            Ok.withSession("user" -> user.id.get.toString)
        }
      }
    )
  }
}
