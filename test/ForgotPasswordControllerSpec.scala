import java.util.UUID
import java.util.concurrent.TimeUnit

import controllers.routes
import models.{User, UserDAO}
import org.joda.time.DateTime
import org.mockito.ArgumentCaptor
import play.api.inject.bind
import org.scalatestplus.play.PlaySpec
import play.api.test.FakeRequest
import play.api.test.Helpers._
import org.mockito.Mockito.{times, verify, when}
import play.api.libs.mailer.{Email, MailerClient}
import utils.{PasswordUtils, TimeTokenUtils}

class ForgotPasswordControllerSpec extends PlaySpec
  with org.scalatest.mockito.MockitoSugar
  with ProjectTestApp {
  private val token = UUID.randomUUID().toString
  private val userDAO = mock[UserDAO]
  private val user = mock[User]
  private val mailerClient = mock[MailerClient]
  private val email = mock[Email]
  private val timeTokenUtil = mock[TimeTokenUtils]
  private val passwordUtils = mock[PasswordUtils]

  private val emailCaptor = ArgumentCaptor.forClass(classOf[String])
  private val tokenCaptor = ArgumentCaptor.forClass(classOf[String])
  private val timeCaptor = ArgumentCaptor.forClass(classOf[Long])
  private val passwordCaptor = ArgumentCaptor.forClass(classOf[String])
  private val saltCaptor = ArgumentCaptor.forClass(classOf[String])

  override def overrideModules = Seq(
    bind[UserDAO].toInstance(userDAO),
    bind[User].toInstance(user),
    bind[MailerClient].toInstance(mailerClient),
    bind[Email].toInstance(email),
    bind[TimeTokenUtils].toInstance(timeTokenUtil),
    bind[PasswordUtils].toInstance(passwordUtils)
  )

  "ForgotPasswordController.sendEmail" should {
    "can send confirmation email" in {
      var time = System.currentTimeMillis()
      when(user.email).thenReturn("olga@gmail.com")
      when(timeTokenUtil.getCurrentTime()).thenReturn(time)
      when(timeTokenUtil.getExpirationTime()).thenReturn(time + TimeUnit.MINUTES.toMillis(10))
      when(timeTokenUtil.getToken()).thenReturn(token)
      when(userDAO.findByEmail(user.email)).thenReturn(Some(user))
      when(userDAO.setControlKey(user.email, token, new DateTime(timeTokenUtil.getExpirationTime()))).thenReturn(true)
      when(mailerClient.send(email)).thenReturn(null)

      val Some(result) = route(app, FakeRequest(POST, routes.ForgotPasswordController.sendEmail().url)
        .withFormUrlEncodedBody("email" -> "olga@gmail.com"))
      status(result) mustBe OK

      verify(userDAO, times(1)).setControlKey(emailCaptor.capture, tokenCaptor.capture, new DateTime(timeCaptor.capture))
      emailCaptor.getValue mustBe "olga@gmail.com"
      tokenCaptor.getValue mustBe token
      timeCaptor.getValue mustBe new DateTime(timeTokenUtil.getExpirationTime())
    }
    "cannot send email because of invalid email" in {
      val Some(result) = route(app, FakeRequest(POST, routes.ForgotPasswordController.sendEmail().url)
        .withFormUrlEncodedBody("email" -> "olga@gmail"))
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe ("{\"email\":[\"Wrong email address.\"]}")
    }
    "cannot send email because of email missing" in {
      val Some(result) = route(app, FakeRequest(POST, routes.ForgotPasswordController.sendEmail().url)
        .withFormUrlEncodedBody())
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe ("{\"email\":[\"This field is required\"]}")
    }
  }

  "ForgotPasswordController.checkEmailPage" should {
    "redirect user to check email page" in {
      val Some(result) = route(app, FakeRequest(GET, routes.ForgotPasswordController.checkEmailPage().url))
      status(result) mustBe OK
    }
  }

  "ForgotPasswordController.changePassword" should {
    "can change password" in {
      val salt = new PasswordUtils().generateRandomString()
      val hashPassword = new PasswordUtils().encryptPassword("qazseszaq", salt)

      when(timeTokenUtil.getCurrentTime()).thenReturn(1519396497487L)
      when(userDAO.findByControlKey(token, new DateTime(timeTokenUtil.getCurrentTime()))).thenReturn(Some(user))
      when(user.id).thenReturn(Some(490L))
      when(user.salt).thenReturn(Some(salt))
      when(passwordUtils.encryptPassword("qazseszaq", user.salt.get)).thenReturn(hashPassword)
      when(userDAO.changePassword(user.id.get, passwordUtils.encryptPassword("qazseszaq", user.salt.get))).thenReturn(true)

      val Some(result) = route(app, FakeRequest(POST, routes.ForgotPasswordController.changePassword(token).url)
        .withFormUrlEncodedBody("new_password" -> "qazseszaq", "confirm_password" -> "qazseszaq"))
      status(result) mustBe OK

      verify(userDAO, times(1)).findByControlKey(tokenCaptor.capture, new DateTime(timeCaptor.capture))
      verify(passwordUtils, times(2)).encryptPassword(passwordCaptor.capture, saltCaptor.capture)
      tokenCaptor.getValue mustBe token
      timeCaptor.getValue mustBe new DateTime(timeTokenUtil.getCurrentTime())
      passwordCaptor.getValue mustBe "qazseszaq"
      saltCaptor.getValue mustBe salt
    }
    "cannot change password because of passwords missing" in {
      val Some(result) = route(app, FakeRequest(POST, routes.ForgotPasswordController.changePassword(token).url)
        .withFormUrlEncodedBody())
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe ("{\"new_password\":[\"This field is required\"]," +
        "\"confirm_password\":[\"This field is required\"]}")
    }
    "cannot change password because of passwords don't match" in {
      val Some(result) = route(app, FakeRequest(POST, routes.ForgotPasswordController.changePassword(token).url)
        .withFormUrlEncodedBody("new_password" -> "eszaqazse", "confirm_password" -> "qazseszaq"))
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe ("{\"\":[\"Passwords don't match\"]}")
    }
  }

  "ForgotPasswordController.forgotPasswordPage" should {
    "redirect user to forgot password page" in {
      when(timeTokenUtil.getCurrentTime()).thenReturn(1519396497487L)
      when(userDAO.findByControlKey(token, new DateTime(timeTokenUtil.getCurrentTime()))).thenReturn(Some(user))

      val Some(result) = route(app, FakeRequest(GET, routes.ForgotPasswordController.forgotPasswordPage(token).url))
      status(result) mustBe OK

      verify(userDAO, times(1)).findByControlKey(tokenCaptor.capture, new DateTime(timeCaptor.capture))
      tokenCaptor.getValue mustBe token
      timeCaptor.getValue mustBe new DateTime(timeTokenUtil.getCurrentTime())
    }
    "don't redirect user to forgot password page" in {
      when(timeTokenUtil.getCurrentTime()).thenReturn(1519396497487L)
      when(userDAO.findByControlKey(token, new DateTime(timeTokenUtil.getCurrentTime()))).thenReturn(None)

      val Some(result) = route(app, FakeRequest(GET, routes.ForgotPasswordController.forgotPasswordPage(token).url))
      status(result) mustBe SEE_OTHER
    }
  }
}
