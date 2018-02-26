import TestUtils._
import controllers.routes
import models.UserDAO
import play.api.inject.bind
import org.scalatestplus.play.PlaySpec
import play.api.test.FakeRequest
import play.api.test.Helpers._
import org.mockito.Mockito.when
import play.api.libs.json.Json
import org.skyscreamer.jsonassert.JSONAssert

class HomeControllerSpec extends PlaySpec
  with org.scalatest.mockito.MockitoSugar
  with ProjectTestApp {
  private val userDAO = mock[UserDAO]

  override def overrideModules = Seq(
    bind[UserDAO].toInstance(userDAO)
  )

  "HomeController.home" should {
    "redirect authorized user to users list page" in {
      val Some(result) = route(app, FakeRequest(GET, routes.HomeController.home().url).withSession(userSession))
      status(result) mustBe OK
    }
    "don't redirect unauthorized user to users list page" in {
      val Some(result) = route(app, FakeRequest(GET, routes.HomeController.home().url))
      status(result) mustBe UNAUTHORIZED
    }
  }

  "HomeController.allUsers" should {

    "get registered users list" in {
      when(userDAO.getAllUsers).thenReturn(Seq())

      val Some(result) = route(app, FakeRequest(GET, routes.HomeController.allUsers().url).withSession(userSession))
      status(result) mustBe OK
      JSONAssert.assertEquals(contentAsString(result), Json.toJson(userDAO.getAllUsers).toString, true)
    }
    "don't get registered users list" in {
      val Some(result) = route(app, FakeRequest(GET, routes.HomeController.allUsers().url))
      status(result) mustBe UNAUTHORIZED
    }
  }
}
