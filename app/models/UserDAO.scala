package models

import javax.inject.Inject

import play.api.db._
import anorm.SqlParser._
import anorm._
import controllers.routes
import org.joda.time.DateTime

trait UserDAO {
  def getUserAvatar(id: Long): String
  def findById(id: Long): Option[User]
  def findByEmail(email: String): Option[User]
  def findByControlKey(controlKey: String, currentTime: DateTime): Option[User]
  def findByFBId(fbId: String): Option[User]
  def create(name: String, email: String, password: String, salt: String, role: String): Option[Long]
  def createFBUser(fbId: String, name: String): Option[Long]
  def getAllUsers: Seq[User]
  def editProfileWithPassword(id: Long, name: String, email: String, password: String): Boolean
  def editProfile(id: Long, name: String, email: String): Boolean
  def changePassword(id: Long, password: String): Boolean
  def setControlKey(email: String, controlKey: String, expirationTime: DateTime): Boolean
  def setEmail(id: Long, email: String): Boolean
  def isEmailExist(currentEmail: String, newEmail: String): Boolean
  def changeAvatarUrl(id: Long, avatarUrl: String): Boolean
  def checkRole(id: Long): String
  def setRole(id: Long, role: String): Boolean
}

class UserDAOImpl @Inject() (@NamedDatabase("default") db: Database) extends UserDAO {
  val table = "users"

  override def getUserAvatar(id: Long): String = {
    val avatarUrl = db.withConnection { implicit connection =>
      val query = s"SELECT * FROM $table WHERE id=$id"
      SQL(query).as(UserParser.single).avatarUrl
    }
    if(avatarUrl.isDefined) "https://s3.amazonaws.com/" + S3FileDetails.bucket + "/" + avatarUrl.get
    else routes.Assets.versioned("images/user-default.png").toString
  }

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

  override def findByFBId(fbId: String): Option[User] = {
    db.withConnection { implicit connection =>
      val query = s"SELECT * FROM $table WHERE fbId='$fbId'"
      SQL(query).as(UserParser.singleOpt)
    }
  }

  override def createFBUser(fbId: String, name: String): Option[Long] = {
    db.withConnection { implicit connection =>
      val query = s"INSERT INTO $table(fbId, name) VALUES" +
        s"('$fbId', '$name')"
      SQL(query).executeInsert()
    }
  }

  override def create(name: String, email: String, password: String, salt: String, role: String): Option[Long] = {
    db.withConnection { implicit connection =>
      val query = s"INSERT INTO $table(name, email, password, salt, role) VALUES" +
        s"('$name', '$email', '$password', '$salt', '$role')"
      SQL(query).executeInsert()
    }
  }

  override def getAllUsers: Seq[User] = {
    db.withConnection { implicit connection =>
      val query = s"SELECT * FROM $table"
      SQL(query).as(UserParser.*).seq
    }
  }

  override def editProfileWithPassword(id: Long, name: String, email: String, password: String): Boolean = {
    db.withConnection { implicit connection =>
      val query = s"UPDATE $table SET name='$name', email='$email', " +
        s"password='$password' WHERE id=$id"
      SQL(query).execute()
    }
  }

  override def editProfile(id: Long, name: String, email: String): Boolean = {
    db.withConnection { implicit connection =>
      val query = s"UPDATE $table SET name='$name', email='$email' WHERE id=$id"
      SQL(query).execute()
    }
  }

  override def changePassword(id: Long, password: String): Boolean = {
    db.withConnection { implicit connection =>
      val query = s"UPDATE $table SET password='$password' WHERE id=$id"
      SQL(query).execute()
    }
  }

  override def setControlKey(email: String, controlKey: String, expirationTime: DateTime): Boolean = {
    db.withConnection { implicit connection =>
      val query = s"UPDATE $table SET controlKey='$controlKey', expirationTime='$expirationTime' WHERE email='$email'"
      SQL(query).execute()
    }
  }

  override def setEmail(id: Long, email: String): Boolean = {
    db.withConnection { implicit connection =>
      val query = s"UPDATE $table SET email='$email' WHERE id=$id"
      SQL(query).execute()
    }
  }

  override def isEmailExist(currentEmail: String, newEmail: String): Boolean = {
    currentEmail != newEmail && findByEmail(newEmail).isDefined
  }

  override def changeAvatarUrl(id: Long, avatarUrl: String): Boolean = {
    db.withConnection { implicit connection =>
      val query = s"UPDATE $table SET avatarUrl='$avatarUrl' WHERE id=$id"
      SQL(query).execute()
    }
  }

  override def checkRole(id: Long): String = {
    db.withConnection { implicit connection =>
      val query = s"SELECT * FROM $table WHERE id=$id"
      SQL(query).as(UserParser.single).role
    }
  }

  override def setRole(id: Long, role: String): Boolean = {
    db.withConnection { implicit connection =>
      val query = s"UPDATE $table SET role='$role' WHERE id=$id"
      SQL(query).execute()
    }
  }

  private val UserParser = for {
    id <- get[Option[Long]]("id")
    fbId <- get[Option[String]]("fbid")
    name <- get[Option[String]]("name")
    email <- get[Option[String]]("email")
    current_password <- get[Option[String]]("password")
    new_password <- get[Option[String]]("password")
    confirm_password <- get[Option[String]]("password")
    salt <- get[Option[String]]("salt")
    controlKey <- get[Option[String]]("controlKey")
    expirationTime <- get[Option[DateTime]]("expirationTime")
    avatarUrl <- get[Option[String]]("avatarUrl")
    role <- get[Option[String]]("role")
  } yield User(id, fbId, name.getOrElse(""), email.getOrElse(""), current_password,
    new_password.getOrElse(""), confirm_password.getOrElse(""), salt, controlKey,
    expirationTime, avatarUrl, role.getOrElse(""))
}