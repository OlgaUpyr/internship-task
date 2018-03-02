package models

import javax.inject.Inject

import play.api.db._
import anorm.SqlParser._
import anorm._
import controllers.routes

trait ProductDAO {
  def getProductImage(id: Long): String
  def findById(id: Long): Option[Product]
  def create(name: String, description: String, price: Double): Option[Long]
  def getAllProducts: Seq[Product]
  def getProductsByUser(userId: Long): Seq[Product]
  def setIsSold(id: Long): Boolean
  def editProductInfo(id: Long, name: String, description: String, price: Double): Boolean
  def changeProductImage(id: Long, productImage: String): Boolean
  def changeOwner(id: Long, userId: Long): Boolean
}

class ProductDAOImpl @Inject() (@NamedDatabase("postgres") db: Database) extends ProductDAO {
  val table = "products"

  override def getProductImage(id: Long): String = {
    val productImage = db.withConnection { implicit connection =>
      val query = s"SELECT * FROM $table WHERE id=$id"
      SQL(query).as(ProductParser.single).productImage
    }
    if(productImage.isDefined) "https://s3.amazonaws.com/" + S3FileDetails.bucket + "/" + productImage.get
    else routes.Assets.versioned("images/default-product.jpg").toString
  }

  override def findById(id: Long): Option[Product] = {
    db.withConnection { implicit connection =>
      val query = s"SELECT * FROM $table WHERE id=$id"
      SQL(query).as(ProductParser.singleOpt)
    }
  }

  override def create(name: String, description: String, price: Double): Option[Long] = {
    val isSold = false
    db.withConnection { implicit connection =>
      val query = s"INSERT INTO $table(name, description, price, issold) VALUES" +
        s"('$name', '$description', $price, $isSold)"
      SQL(query).executeInsert()
    }
  }

  override def getAllProducts: Seq[Product] = {
    db.withConnection { implicit connection =>
      val query = s"SELECT * FROM $table WHERE issold=false"
      SQL(query).as(ProductParser.*).seq
    }
  }

  override def getProductsByUser(userId: Long): Seq[Product] = {
    db.withConnection { implicit connection =>
      val query = s"SELECT * FROM $table WHERE userid=$userId"
      SQL(query).as(ProductParser.*).seq
    }
  }

  override def setIsSold(id: Long): Boolean = {
    db.withConnection { implicit connection =>
      val isSold = true
      val query = s"UPDATE $table SET issold=$isSold WHERE id=$id"
      SQL(query).execute()
    }
  }

  override def editProductInfo(id: Long, name: String, description: String, price: Double): Boolean = {
    db.withConnection { implicit connection =>
      val query = s"UPDATE $table SET name='$name', description='$description', price='$price' WHERE id=$id"
      SQL(query).execute()
    }
  }

  override def changeProductImage(id: Long, productImage: String): Boolean = {
    db.withConnection { implicit connection =>
      val query = s"UPDATE $table SET productImage='$productImage' WHERE id=$id"
      SQL(query).execute()
    }
  }

  override def changeOwner(id: Long, userId: Long): Boolean = {
    println("id="+id)
    println("userId="+userId)
    db.withConnection { implicit connection =>
      val query = s"UPDATE $table SET userid=$userId WHERE id=$id"
      SQL(query).execute()
    }
  }

  private val ProductParser = for {
    id <- get[Option[Long]]("id")
    userId <- get[Option[Long]]("userid")
    name <- get[Option[String]]("name")
    description <- get[Option[String]]("description")
    price <- get[Option[Double]]("price")
    productImage <- get[Option[String]]("productImage")
    isSold <- get[Option[Boolean]]("isSold")
  } yield Product(id, userId, name.getOrElse(""), description.getOrElse(""), price.getOrElse(0), productImage, isSold)
}