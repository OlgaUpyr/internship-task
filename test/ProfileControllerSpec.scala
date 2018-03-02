import TestUtils._
import controllers.routes
import models.{User, UserDAO}
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.{times, verify, when}
import play.api.inject.bind
import org.scalatestplus.play.PlaySpec
import org.skyscreamer.jsonassert.JSONAssert
import play.api.libs.Files.{SingletonTemporaryFileCreator, TemporaryFile}
import play.api.libs.json.Json
import play.api.mvc.MultipartFormData
import play.api.mvc.MultipartFormData.{BadPart, FilePart}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.PasswordUtils

class ProfileControllerSpec extends PlaySpec
  with org.scalatest.mockito.MockitoSugar
  with ProjectTestApp {
  private val userDAO = mock[UserDAO]
  private val user = mock[User]
  private val passwordUtils = mock[PasswordUtils]

  private val idCaptor = ArgumentCaptor.forClass(classOf[Long])
  private val nameCaptor = ArgumentCaptor.forClass(classOf[String])
  private val emailCaptor = ArgumentCaptor.forClass(classOf[String])
  private val passwordCaptor = ArgumentCaptor.forClass(classOf[String])
  private val currentPasswordCaptor = ArgumentCaptor.forClass(classOf[String])
  private val saltCaptor = ArgumentCaptor.forClass(classOf[String])

  override def overrideModules = Seq(
    bind[UserDAO].toInstance(userDAO),
    bind[User].toInstance(user),
    bind[PasswordUtils].toInstance(passwordUtils)
  )

  "ProfileController.updateUserInfo" should {

    val salt = new PasswordUtils().generateRandomString()
    val hashPassword = new PasswordUtils().encryptPassword("qazseszaq", salt)

    "successful user profile update (all fields are changed)" in {
      when(userDAO.findById(490L)).thenReturn(Some(user))
      when(user.role).thenReturn("customer")
      when(userDAO.setRole(490L, user.role)).thenReturn(true)
      when(user.currentPassword).thenReturn(Some("eszaqazse"))
      when(user.salt).thenReturn(Some(salt))
      when(user.newPassword).thenReturn("qazseszaq")
      when(passwordUtils.passwordsMatch(user.currentPassword.get, user.salt.get, user.newPassword)).thenReturn(true)
      when(user.id).thenReturn(Some(490L))
      when(user.name).thenReturn("Olga Upyr")
      when(user.email).thenReturn("olga@gmail.com")
      when(userDAO.editProfileWithPassword(user.id.get, user.name, user.email,
        new PasswordUtils().encryptPassword(user.newPassword, user.salt.get))).thenReturn(true)

      val files = Seq[FilePart[TemporaryFile]](FilePart("file", "test.png", Some("image/jpg"),
        SingletonTemporaryFileCreator.create("test", "jpg")))
      val data = Map("name" -> Seq("Olga Upyr"),
        "email" -> Seq("olga@gmail.com"),
        "current_password" -> Seq("eszaqazse"),
        "new_password" -> Seq("qazseszaq"),
        "confirm_password" -> Seq("qazseszaq"))
      val Some(result) = route(app, FakeRequest(POST, routes.ProfileController.updateUserInfo(490L).url)
        .withSession(userSession)
        .withMultipartFormDataBody(MultipartFormData(data, files, Seq[BadPart]())))
      status(result) mustBe OK

      verify(userDAO, times(1)).editProfileWithPassword(idCaptor.capture, nameCaptor.capture, emailCaptor.capture, passwordCaptor.capture)
      idCaptor.getValue mustBe 490L
      emailCaptor.getValue mustBe "olga@gmail.com"
      nameCaptor.getValue mustBe "Olga Upyr"
      passwordCaptor.getValue mustBe passwordUtils.encryptPassword("qazseszaq", salt)
    }
    "successful user profile update (name and email are changed)" in {
      when(userDAO.findById(490L)).thenReturn(Some(user))
      when(user.id).thenReturn(Some(490L))
      when(user.name).thenReturn("Olga Upyr")
      when(user.email).thenReturn("olga@gmail.com")
      when(userDAO.findByEmail(user.email)).thenReturn(None)
      when(userDAO.editProfile(user.id.get, user.name, user.email)).thenReturn(true)

      val data = Map("name" -> Seq("Olga Upyr"),
        "email" -> Seq("olga@gmail.com"),
        "current_password" -> Seq(""),
        "new_password" -> Seq(""),
        "confirm_password" -> Seq(""))
      val Some(result) = route(app, FakeRequest(POST, routes.ProfileController.updateUserInfo(490L).url)
        .withSession(userSession)
        .withMultipartFormDataBody(MultipartFormData(data, Seq[FilePart[TemporaryFile]](), Seq[BadPart]())))
      status(result) mustBe OK

      verify(userDAO, times(1)).editProfile(idCaptor.capture, nameCaptor.capture, emailCaptor.capture)
      idCaptor.getValue mustBe 490L
      emailCaptor.getValue mustBe "olga@gmail.com"
      nameCaptor.getValue mustBe "Olga Upyr"
    }
    "failed user profile update because of name missing" in {
      val data = Map("name" -> Seq(""),
        "email" -> Seq("olga@gmail.com"),
        "current_password" -> Seq(""),
        "new_password" -> Seq(""),
        "confirm_password" -> Seq(""))
      val Some(result) = route(app, FakeRequest(POST, routes.ProfileController.updateUserInfo(490L).url)
        .withSession(userSession)
        .withMultipartFormDataBody(MultipartFormData(data, Seq[FilePart[TemporaryFile]](), Seq[BadPart]())))
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe ("{\"name\":[\"This field is required\"]}")
    }
    "failed user profile update because of email missing" in {
      val data = Map("name" -> Seq("Olga Upyr"),
        "email" -> Seq(""),
        "current_password" -> Seq(""),
        "new_password" -> Seq(""),
        "confirm_password" -> Seq(""))
      val Some(result) = route(app, FakeRequest(POST, routes.ProfileController.updateUserInfo(490L).url)
        .withSession(userSession)
        .withMultipartFormDataBody(MultipartFormData(data, Seq[FilePart[TemporaryFile]](), Seq[BadPart]())))
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe ("{\"email\":[\"This field is required\",\"Wrong email address.\"]}")
    }
    "failed user profile update because of passwords missing" in {
      val data = Map("name" -> Seq("Olga Upyr"),
        "email" -> Seq("olga@gmail.com"),
        "current_password" -> Seq("eszaqazse"),
        "new_password" -> Seq(""),
        "confirm_password" -> Seq(""))
      val Some(result) = route(app, FakeRequest(POST, routes.ProfileController.updateUserInfo(490L).url)
        .withSession(userSession)
        .withMultipartFormDataBody(MultipartFormData(data, Seq[FilePart[TemporaryFile]](), Seq[BadPart]())))
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe ("{\"\":[\"Missing fields.\"]}")
    }
    "failed user profile update because of current password missing" in {
      val data = Map("name" -> Seq("Olga Upyr"),
        "email" -> Seq("olga@gmail.com"),
        "current_password" -> Seq(""),
        "new_password" -> Seq("qazseszaq"),
        "confirm_password" -> Seq("qazseszaq"))
      val Some(result) = route(app, FakeRequest(POST, routes.ProfileController.updateUserInfo(490L).url)
        .withSession(userSession)
        .withMultipartFormDataBody(MultipartFormData(data, Seq[FilePart[TemporaryFile]](), Seq[BadPart]())))
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe ("{\"\":[\"Missing fields.\"]}")
    }
    "failed user profile update because of invalid email" in {
      val data = Map("name" -> Seq("Olga Upyr"),
        "email" -> Seq("olga@gmail"),
        "current_password" -> Seq(""),
        "new_password" -> Seq(""),
        "confirm_password" -> Seq(""))
      val Some(result) = route(app, FakeRequest(POST, routes.ProfileController.updateUserInfo(490L).url)
        .withSession(userSession)
        .withMultipartFormDataBody(MultipartFormData(data, Seq[FilePart[TemporaryFile]](), Seq[BadPart]())))
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe ("{\"email\":[\"Wrong email address.\"]}")
    }
    "failed registration because of email is already registered" in {
      when(user.email).thenReturn("olga.upyr@gmail.com")
      when(userDAO.isEmailExist(user.email, "olga@gmail.com")).thenReturn(true)

      val data = Map("name" -> Seq("Olga Upyr"),
        "email" -> Seq("olga@gmail.com"),
        "current_password" -> Seq(""),
        "new_password" -> Seq(""),
        "confirm_password" -> Seq(""))
      val Some(result) = route(app, FakeRequest(POST, routes.ProfileController.updateUserInfo(490L).url)
        .withSession(userSession)
        .withMultipartFormDataBody(MultipartFormData(data, Seq[FilePart[TemporaryFile]](), Seq[BadPart]())))
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe ("{\"email\":[\"The email address you have entered is already registered.\"]}")
    }
    "failed registration because of password is too short" in {
      val data = Map("name" -> Seq("Olga Upyr"),
        "email" -> Seq("olga@gmail.com"),
        "current_password" -> Seq("eszaqazse"),
        "new_password" -> Seq("qaz"),
        "confirm_password" -> Seq("qaz"))
      val Some(result) = route(app, FakeRequest(POST, routes.ProfileController.updateUserInfo(490L).url)
        .withSession(userSession)
        .withMultipartFormDataBody(MultipartFormData(data, Seq[FilePart[TemporaryFile]](), Seq[BadPart]())))
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe ("{\"new_password\":[\"Password must be at least 8 characters in length.\"]," +
        "\"confirm_password\":[\"Password must be at least 8 characters in length.\"]}")
    }
    "failed registration because of passwords do not match" in {
      val data = Map("name" -> Seq("Olga Upyr"),
        "email" -> Seq("olga@gmail.com"),
        "current_password" -> Seq("eszaqazse"),
        "new_password" -> Seq("123456780"),
        "confirm_password" -> Seq("qazseszaq"))
      val Some(result) = route(app, FakeRequest(POST, routes.ProfileController.updateUserInfo(490L).url)
        .withSession(userSession)
        .withMultipartFormDataBody(MultipartFormData(data, Seq[FilePart[TemporaryFile]](), Seq[BadPart]())))
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe ("{\"\":[\"Passwords don't match\"]}")
    }
    "failed registration because of current password does not match entered password" in {
      when(user.email).thenReturn("olga@gmail.com")
      when(userDAO.findById(490L)).thenReturn(Some(user))
      when(user.salt).thenReturn(Some(salt))
      when(user.newPassword).thenReturn("eszaqazse")
      when(passwordUtils.passwordsMatch("1234567890", user.salt.get, user.newPassword)).thenReturn(false)

      val data = Map("name" -> Seq("Olga Upyr"),
        "email" -> Seq("olga@gmail.com"),
        "current_password" -> Seq("eszaqazse"),
        "new_password" -> Seq("qazseszaq"),
        "confirm_password" -> Seq("qazseszaq"))
      val Some(result) = route(app, FakeRequest(POST, routes.ProfileController.updateUserInfo(490L).url)
        .withSession(userSession)
        .withMultipartFormDataBody(MultipartFormData(data, Seq[FilePart[TemporaryFile]](), Seq[BadPart]())))
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe ("{\"current_password\":[\"The password you have entered does not match your current one.\"]}")
    }
  }

  "ProfileController.editProfile" should {
    "redirect authorized user to edit profile page" in {
      val Some(result) = route(app, FakeRequest(GET, routes.ProfileController.editProfile().url).withSession(userSession))
      status(result) mustBe OK
    }
    "don't redirect unauthorized user to edit profile page" in {
      val Some(result) = route(app, FakeRequest(GET, routes.ProfileController.editProfile().url))
      status(result) mustBe SEE_OTHER
    }
  }

  "ProfileController.profileInfo" should {
    "get edit profile page info" in {
      when(userDAO.findById(490L)).thenReturn(Some(user))

      val Some(result) = route(app, FakeRequest(GET, routes.ProfileController.profileInfo().url).withSession(userSession))
      status(result) mustBe OK
      JSONAssert.assertEquals(contentAsString(result), Json.toJson(user).toString, true)
    }
    "don't get edit profile page info" in {
      val Some(result) = route(app, FakeRequest(GET, routes.ProfileController.profileInfo().url))
      status(result) mustBe UNAUTHORIZED
    }
  }
}
