import controllers.routes
import models.{User, UserDAO}
import play.api.inject.bind
import org.scalatestplus.play.PlaySpec
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.libs.Json



class RegistrationControllerSpec extends PlaySpec
  with org.scalatest.mockito.MockitoSugar {
  private val userDAO = mock[UserDAO]

  implicit lazy val app = new GuiceApplicationBuilder()
    .overrides(bind[UserDAO].toInstance(userDAO))
    .build()

  "RegistrationController.register" should {
    "1" in {
      val Some(result) = route(app, FakeRequest(POST, routes.RegistrationController.register().url)
        .withFormUrlEncodedBody("id" -> null, "name" -> "Some Name", "email" -> "registrationTest@gmail.com",
          "password" -> "122333qwerty", "confirm_password" -> "122333qwerty"))
      status(result) mustBe OK
      val user = Json.fromJson[User](contentAsJson(result)).get
      user.content.length must not be 0
      analysesResponse.header.length must not be 0
      analysesResponse.content.head.length mustBe analysesResponse.header.length
    }
  }
}
