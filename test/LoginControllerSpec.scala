import TestUtils._
import controllers.routes
import models.{User, UserDAO}
import org.mockito.ArgumentCaptor
import org.scalatest.BeforeAndAfter
import play.api.inject.bind
import org.scalatestplus.play.PlaySpec
import play.api.test.FakeRequest
import play.api.test.Helpers._
import org.mockito.Mockito.{times, verify, when}
import utils.PasswordUtils

class LoginControllerSpec extends PlaySpec
  with org.scalatest.mockito.MockitoSugar
  with ProjectTestApp with BeforeAndAfter{
  private val userDAO = mock[UserDAO]
  private val user = mock[User]
  private val passwordUtils = mock[PasswordUtils]

  private val emailCaptor = ArgumentCaptor.forClass(classOf[String])
  private val passwordCaptor = ArgumentCaptor.forClass(classOf[String])
  private val currentPasswordCaptor = ArgumentCaptor.forClass(classOf[String])
  private val saltCaptor = ArgumentCaptor.forClass(classOf[String])

  override def overrideModules = Seq(
    bind[UserDAO].toInstance(userDAO),
    bind[User].toInstance(user),
    bind[PasswordUtils].toInstance(passwordUtils)
  )

  "LoginController.login" should {

    val salt = new PasswordUtils().generateRandomString()

    "successful login" in {
      when(userDAO.findByEmail("olga@gmail.com")).thenReturn(Some(user))
      when(user.newPassword).thenReturn(new PasswordUtils().encryptPassword("qazseszaq", salt))
      when(user.salt).thenReturn(Some(salt))
      when(user.id).thenReturn(Some(490L))
      when(user.role).thenReturn(Some("customer"))
      when(userDAO.checkRole(user.id.get)).thenReturn("customer")
      when(passwordUtils.passwordsMatch("qazseszaq", user.salt.get, user.newPassword)).thenReturn(true)

      val Some(result) = route(app, FakeRequest(POST, routes.LoginController.login().url)
        .withFormUrlEncodedBody("email" -> "olga@gmail.com",
          "password" -> "qazseszaq",
          "role" -> "customer"))
      status(result) mustBe OK

      verify(userDAO, times(1)).findByEmail(emailCaptor.capture)
      emailCaptor.getValue mustBe "olga@gmail.com"
      verify(passwordUtils, times(1)).passwordsMatch(currentPasswordCaptor.capture, saltCaptor.capture, passwordCaptor.capture)
      currentPasswordCaptor.getValue mustBe "qazseszaq"
      saltCaptor.getValue mustBe salt
      passwordCaptor.getValue mustBe new PasswordUtils().encryptPassword("qazseszaq", salt)
    }
    "failed login because of email missing" in {
      val Some(result) = route(app, FakeRequest(POST, routes.LoginController.login().url)
        .withFormUrlEncodedBody("password" -> "qazseszaq", "role" -> "customer"))
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe ("{\"email\":[\"This field is required\"]}")
    }
    "failed login because of password missing" in {
      val Some(result) = route(app, FakeRequest(POST, routes.LoginController.login().url)
        .withFormUrlEncodedBody("email" -> "olga@gmail.com", "role" -> "customer"))
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe ("{\"password\":[\"This field is required\"]}")
    }
    "failed login because of unregistered email" in {
      when(userDAO.findByEmail("email@gmail.com")).thenReturn(None)

      val Some(result) = route(app, FakeRequest(POST, routes.LoginController.login().url)
        .withFormUrlEncodedBody("email" -> "email@gmail.com", "password" -> "qazseszaq", "role" -> "customer"))
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe ("{\"email\":[\"Wrong email address.\"]}")
    }
    "failed login because of wrong password" in {
      when(userDAO.findByEmail("olga@gmail.com")).thenReturn(Some(user))
      when(user.newPassword).thenReturn(new PasswordUtils().encryptPassword("qazseszaq", salt))
      when(user.salt).thenReturn(Some(salt))
      when(userDAO.checkRole(490L)).thenReturn("customer")
      when(user.role).thenReturn(Some("customer"))
      when(passwordUtils.passwordsMatch("1234567890", user.salt.get, user.newPassword)).thenReturn(false)

      val Some(result) = route(app, FakeRequest(POST, routes.LoginController.login().url)
        .withFormUrlEncodedBody("email" -> "olga@gmail.com", "password" -> "1234567890", "role" -> "customer"))
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe ("{\"password\":[\"Password is invalid.\"]}")
    }
  }

  "LoginController.loginPage" should {
    "redirect authorized user to users list from login page" in {
      val Some(result) = route(app, FakeRequest(GET, routes.LoginController.loginPage().url).withSession(userSession))
      status(result) mustBe SEE_OTHER
    }
    "don't redirect unauthorized user to users list from login page" in {
      val Some(result) = route(app, FakeRequest(GET, routes.LoginController.loginPage().url))
      status(result) mustBe OK
    }
  }

  "LoginController.logout" should {
    "redirect authorized user to login page from profile" in {
      val Some(result) = route(app, FakeRequest(GET, routes.LoginController.logout().url).withSession(userSession))
      status(result) mustBe SEE_OTHER
    }
    "accept unauthorized user on login page" in {
      val Some(result) = route(app, FakeRequest(GET, routes.LoginController.logout().url))
      status(result) mustBe OK
    }
  }
}
