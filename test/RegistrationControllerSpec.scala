import TestUtils._
import utils.PasswordUtils
import controllers.routes
import models.{User, UserDAO}
import org.mockito.ArgumentCaptor
import play.api.inject.bind
import org.scalatestplus.play.PlaySpec
import play.api.libs.Files.{SingletonTemporaryFileCreator, TemporaryFile}
import play.api.mvc.MultipartFormData
import play.api.mvc.MultipartFormData.{BadPart, FilePart}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import org.mockito.Mockito._

class RegistrationControllerSpec extends PlaySpec
  with org.scalatest.mockito.MockitoSugar
  with ProjectTestApp {
  private val userDAO = mock[UserDAO]
  private val user = mock[User]
  private val passwordUtils = mock[PasswordUtils]

  private val nameCaptor = ArgumentCaptor.forClass(classOf[String])
  private val emailCaptor = ArgumentCaptor.forClass(classOf[String])
  private val passwordCaptor = ArgumentCaptor.forClass(classOf[String])
  private val saltCaptor = ArgumentCaptor.forClass(classOf[String])
  private val roleCaptor = ArgumentCaptor.forClass(classOf[String])

  override def overrideModules = Seq(
    bind[UserDAO].toInstance(userDAO),
    bind[User].toInstance(user),
    bind[PasswordUtils].toInstance(passwordUtils)
  )

  "RegistrationController.register" should {

    val salt = new PasswordUtils().generateRandomString()
    val hashPassword = new PasswordUtils().encryptPassword("qazseszaq", salt)

    "successful registration with profile image" in {
      when(userDAO.findByEmail("olga@gmail.com")).thenReturn(None)
      when(passwordUtils.generateRandomString()).thenReturn(salt)
      when(user.name).thenReturn("Olga Upyr")
      when(user.email).thenReturn("olga@gmail.com")
      when(passwordUtils.encryptPassword("qazseszaq", salt)).thenReturn(hashPassword)
      when(user.salt).thenReturn(Some(salt))
      when(user.role).thenReturn("seller")
      when(userDAO.create
      (user.name, user.email, hashPassword, salt, user.role)).thenReturn(Some(490L))
      when(userDAO.findById(490L)).thenReturn(Some(user))

      val files = Seq[FilePart[TemporaryFile]](FilePart("file", "test.png", Some("image/png"),
        SingletonTemporaryFileCreator.create("test", "png")))
      val data = Map("name" -> Seq("Olga Upyr"),
        "email" -> Seq("olga@gmail.com"),
        "new_password" -> Seq("qazseszaq"),
        "confirm_password" -> Seq("qazseszaq"),
        "role" -> Seq("seller"))
      val Some(result) = route(app, FakeRequest(POST, routes.RegistrationController.register().url)
        .withMultipartFormDataBody(MultipartFormData(data, files, Seq[BadPart]())))
      status(result) mustBe OK

      verify(userDAO, times(1)).create(nameCaptor.capture, emailCaptor.capture, passwordCaptor.capture,
        saltCaptor.capture, roleCaptor.capture)
      nameCaptor.getValue mustBe "Olga Upyr"
      emailCaptor.getValue mustBe "olga@gmail.com"
      passwordCaptor.getValue mustBe new PasswordUtils().encryptPassword("qazseszaq", saltCaptor.getValue)
      saltCaptor.getValue mustBe salt
      roleCaptor.getValue mustBe "seller"
    }
    "successful registration without profile image" in {
      when(userDAO.findByEmail("olga@gmail.com")).thenReturn(None)
      when(passwordUtils.generateRandomString()).thenReturn(salt)
      when(user.name).thenReturn("Olga Upyr")
      when(user.email).thenReturn("olga@gmail.com")
      when(passwordUtils.encryptPassword("qazseszaq", salt)).thenReturn(hashPassword)
      when(user.salt).thenReturn(Some(salt))
      when(user.role).thenReturn("customer")
      when(userDAO.create(user.name, user.email, hashPassword, salt, user.role)).thenReturn(Some(490L))
      when(userDAO.findById(490L)).thenReturn(Some(user))

      val data = Map("name" -> Seq("Olga Upyr"),
        "email" -> Seq("olga@gmail.com"),
        "new_password" -> Seq("qazseszaq"),
        "confirm_password" -> Seq("qazseszaq"),
        "role" -> Seq("customer"))
      val Some(result) = route(app, FakeRequest(POST, routes.RegistrationController.register().url)
        .withMultipartFormDataBody(MultipartFormData(data, Seq[FilePart[TemporaryFile]](), Seq[BadPart]())))
      status(result) mustBe OK
    }
    "failed registration because of name missing" in {
      val data = Map("email" -> Seq("olga@gmail.com"),
        "new_password" -> Seq("qazseszaq"),
        "confirm_password" -> Seq("qazseszaq"),
        "role" -> Seq("customer"))
      val Some(result) = route(app, FakeRequest(POST, routes.RegistrationController.register().url)
        .withMultipartFormDataBody(MultipartFormData(data, Seq[FilePart[TemporaryFile]](), Seq[BadPart]())))
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe ("{\"name\":[\"This field is required\"]}")
    }
    "failed registration because of email missing" in {
      val data = Map("name" -> Seq("Olga Upyr"),
        "new_password" -> Seq("qazseszaq"),
        "confirm_password" -> Seq("qazseszaq"),
        "role" -> Seq("customer"))
      val Some(result) = route(app, FakeRequest(POST, routes.RegistrationController.register().url)
        .withMultipartFormDataBody(MultipartFormData(data, Seq[FilePart[TemporaryFile]](), Seq[BadPart]())))
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe ("{\"email\":[\"This field is required\"]}")
    }
    "failed registration because of password missing" in {
      val data = Map("name" -> Seq("Olga Upyr"),
        "email" -> Seq("olga@gmail.com"),
        "confirm_password" -> Seq("qazseszaq"),
        "role" -> Seq("customer"))
      val Some(result) = route(app, FakeRequest(POST, routes.RegistrationController.register().url)
        .withMultipartFormDataBody(MultipartFormData(data, Seq[FilePart[TemporaryFile]](), Seq[BadPart]())))
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe ("{\"new_password\":[\"This field is required\"]}")
    }
    "failed registration because of confirm password missing" in {
      val data = Map("name" -> Seq("Olga Upyr"),
        "email" -> Seq("olga@gmail.com"),
        "new_password" -> Seq("qazseszaq"),
        "role" -> Seq("customer"))
      val Some(result) = route(app, FakeRequest(POST, routes.RegistrationController.register().url)
        .withMultipartFormDataBody(MultipartFormData(data, Seq[FilePart[TemporaryFile]](), Seq[BadPart]())))
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe ("{\"confirm_password\":[\"This field is required\"]}")
    }
    "failed registration because of invalid email" in {
      val data = Map("name" -> Seq("Olga Upyr"),
        "email" -> Seq("olga@gmail"),
        "new_password" -> Seq("qazseszaq"),
        "confirm_password" -> Seq("qazseszaq"),
        "role" -> Seq("customer"))
      val Some(result) = route(app, FakeRequest(POST, routes.RegistrationController.register().url)
        .withMultipartFormDataBody(MultipartFormData(data, Seq[FilePart[TemporaryFile]](), Seq[BadPart]())))
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe ("{\"email\":[\"Wrong email address.\"]}")
    }
    "failed registration because of email is already registered" in {
      when(userDAO.findByEmail("olga@gmail.com")).thenReturn(Some(user))

      val data = Map("name" -> Seq("Olga Upyr"),
        "email" -> Seq("olga@gmail.com"),
        "new_password" -> Seq("qazseszaq"),
        "confirm_password" -> Seq("qazseszaq"),
        "role" -> Seq("customer"))
      val Some(result) = route(app, FakeRequest(POST, routes.RegistrationController.register().url)
        .withMultipartFormDataBody(MultipartFormData(data, Seq[FilePart[TemporaryFile]](), Seq[BadPart]())))
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe ("{\"email\":[\"The email address you have entered is already registered.\"]}")
    }
    "failed registration because of password is too short" in {
      val data = Map("name" -> Seq("Olga Upyr"),
        "email" -> Seq("olga@gmail.com"),
        "new_password" -> Seq("1234"),
        "confirm_password" -> Seq("1234"),
        "role" -> Seq("customer"))
      val Some(result) = route(app, FakeRequest(POST, routes.RegistrationController.register().url)
        .withMultipartFormDataBody(MultipartFormData(data, Seq[FilePart[TemporaryFile]](), Seq[BadPart]())))
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe ("{\"new_password\":[\"Password must be at least 8 characters in length.\"]," +
        "\"confirm_password\":[\"Password must be at least 8 characters in length.\"]}")
    }
    "failed registration because of passwords do not match" in {
      val data = Map("name" -> Seq("Olga Upyr"),
        "email" -> Seq("olga@gmail.com"),
        "new_password" -> Seq("12345678"),
        "confirm_password" -> Seq("87654321"),
        "role" -> Seq("customer"))
      val Some(result) = route(app, FakeRequest(POST, routes.RegistrationController.register().url)
        .withMultipartFormDataBody(MultipartFormData(data, Seq[FilePart[TemporaryFile]](), Seq[BadPart]())))
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe ("{\"\":[\"Passwords don't match\"]}")
    }
  }

  "RegistrationController.registrationPage" should {
    "redirect authorized user to users list from registration page" in {
      val Some(result) = route(app, FakeRequest(GET, routes.RegistrationController.registrationPage().url).withSession(userSession))
      status(result) mustBe SEE_OTHER
    }
    "don't redirect unauthorized user to users list from registration page" in {
      val Some(result) = route(app, FakeRequest(GET, routes.RegistrationController.registrationPage().url))
      status(result) mustBe OK
    }
  }
}
