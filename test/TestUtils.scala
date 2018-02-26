import models.User

object TestUtils {
  val userSession = "user" -> 490L.toString

  var testDatabaseUrl = "jdbc:postgresql://localhost/userdb"
  var testDatabaseUsername = "postgres"
  var testDatabasePassword = "gre"
}
