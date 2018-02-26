import TestUtils._
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.guice.GuiceableModule
import play.api.test._
import org.scalatest._

trait ProjectTestApp extends TestSuiteMixin {
  this: TestSuite =>

  val defaultConf = Map(
    "db.postgres.url" -> testDatabaseUrl,
    "db.postgres.username" -> testDatabaseUsername,
    "db.postgres.password" -> testDatabasePassword
  )

  def overrideModules: Seq[GuiceableModule] = Nil

  def newAppForTest(testData: TestData): Application =
    new GuiceApplicationBuilder()
      .configure(defaultConf)
      .overrides(overrideModules: _*)
      .build

  private var appPerTest: Application = _
  implicit final def app: Application = synchronized { appPerTest }

  abstract override def withFixture(test: NoArgTest): Outcome = {
    synchronized { appPerTest = newAppForTest(test) }
    Helpers.running(app) {
      super.withFixture(test)
    }
  }
}