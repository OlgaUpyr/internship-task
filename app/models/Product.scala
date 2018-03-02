package models

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json._

case class Product(id: Option[Long], userId: Option[Long], name: String, description: String,
                   price: Double, productImage: Option[String], isSold: Option[Boolean])

object Product {

  implicit object ProductFormat extends Format[Product] {
    def writes(product: Product): JsValue = {
      val productSeq = Seq(
        "name" -> JsString(product.name),
        "description" -> JsString(product.description),
        "price" -> JsNumber(product.price)
      )
      JsObject(productSeq)
    }

    def reads(json: JsValue): JsResult[Product] = {
      JsSuccess(Product(null, null, "", "", 0, null, null))
    }
  }
}

object ProductForm {
  val productForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "price" -> nonEmptyText
    )((a, b, c) => Product(None, None, a, b, c.toDouble, None, None))
    (p => Some(p.name, p.description, p.price.toString))
  )
}