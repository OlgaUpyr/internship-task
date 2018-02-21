package models

import javax.inject.Inject

import play.api.db._
import anorm.SqlParser._
import anorm._
import org.joda.time.DateTime

trait UserDAO {
  def findById(id: Long): Option[User]
  def findByEmail(email: String): Option[User]
  def findByControlKey(controlKey: String, currentTime: DateTime): Option[User]
  def create(name: String, email: String, password: String, salt: String): Option[Long]
  def getAllUsers: Seq[User]
  def editProfileWithPassword(id: Long, name: String, email: String, password: String): Unit
  def editProfile(id: Long, name: String, email: String): Unit
  def changePassword(id: Long, password: String): Unit
  def setControlKey(email: String, controlKey: String, expirationTime: DateTime): Unit
}

class UserDAOImpl @Inject() (@NamedDatabase("postgres") db: Database) extends UserDAO {
  val table = "users"

  override def findById(id: Long): Option[User] = {
    db.withConnection { implicit connection =>
      val query = s"SELECT * FROM $table WHERE id=$id"
      SQL(query).as(UserParser.singleOpt)
    }
  }

  override def findByEmail(email: String): Option[User] = {
    db.withConnection { implicit connection =>
      val query = s"SELECT * FROM $table WHERE email='$email'"
      SQL(query).as(UserParser.singleOpt)
    }
  }

  override def findByControlKey(controlKey: String, currentTime: DateTime): Option[User] = {
    db.withConnection { implicit connection =>
      val query = s"SELECT * FROM $table WHERE controlKey='$controlKey' AND expirationTime > '$currentTime'"
      SQL(query).as(UserParser.singleOpt)
    }
  }

  override def create(name: String, email: String, password: String, salt: String): Option[Long] = {
    db.withConnection { implicit connection =>
      val query = s"INSERT INTO $table(name, email, password, salt) VALUES" +
        s"('$name', '$email', '$password', '$salt')"
      SQL(query).executeInsert()
    }
  }

  override def getAllUsers: Seq[User] = {
    db.withConnection { implicit connection =>
      val query = s"SELECT * FROM $table"
      SQL(query).as(UserParser.*).seq
    }
  }

  override def editProfileWithPassword(id: Long, name: String, email: String, password: String): Unit = {
    db.withConnection { implicit connection =>
      val query = s"UPDATE $table SET name='$name', email='$email', " +
        s"password='$password' WHERE id=$id"
      SQL(query).execute()
    }
  }

  override def editProfile(id: Long, name: String, email: String): Unit = {
    db.withConnection { implicit connection =>
      val query = s"UPDATE $table SET name='$name', email='$email' WHERE id=$id"
      SQL(query).execute()
    }
  }

  override def changePassword(id: Long, password: String): Unit = {
    db.withConnection { implicit connection =>
      val query = s"UPDATE $table SET password='$password' WHERE id=$id"
      SQL(query).execute()
    }
  }

  override def setControlKey(email: String, controlKey: String, expirationTime: DateTime): Unit = {
    db.withConnection { implicit connection =>
      val query = s"UPDATE $table SET controlKey='$controlKey', expirationTime='$expirationTime' WHERE email='$email'"
      SQL(query).execute()
    }
  }

  private val UserParser = for {
    id <- get[Option[Long]]("id")
    name <- get[Option[String]]("name")
    email <- get[Option[String]]("email")
    current_password <- get[Option[String]]("password")
    new_password <- get[Option[String]]("password")
    confirm_password <- get[Option[String]]("password")
    salt <- get[Option[String]]("salt")
    controlKey <- get[Option[String]]("controlKey")
    expirationTime <- get[Option[DateTime]]("expirationTime")
  } yield User(id, name.getOrElse(""), email.getOrElse(""), current_password,
    new_password.getOrElse(""), confirm_password.getOrElse(""), salt, controlKey, expirationTime)
}