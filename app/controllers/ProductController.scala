package controllers

import java.io.File
import java.nio.file.{Files, Paths}
import java.util.UUID
import javax.inject._

import models._
import play.api.libs.json.Json
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc._
import utils.{FileUtils, ImageMagickUtils}

import scala.concurrent.ExecutionContext

@Singleton
class ProductController @Inject()(cc: ControllerComponents, productDAO: ProductDAO, userDAO: UserDAO)
                                 (implicit ec: ExecutionContext)
  extends AbstractController(cc) with play.api.i18n.I18nSupport {
  def productDetailsPage(productId: Long) = Action { implicit request =>
    request.session.get("user").map { user =>
      val product = productDAO.findById(productId).get
      Ok(views.html.productdetail(product, userDAO.findById(product.userId.get).get, userDAO.checkRole(user.toLong), productDAO))
    }.getOrElse{
      Redirect(routes.HomeController.home())
    }
  }

  def productsListForUserPage() = Action { implicit request =>
    request.session.get("user").map { user =>
      Ok(views.html.productslistforuser(productDAO, productDAO.getProductsByUser(user.toLong), userDAO.findById(user.toLong).get.role))
    }.getOrElse{
      Redirect(routes.HomeController.home())
    }
  }

  //seller
  def addProductPage() = Action { implicit request =>
    request.session.get("user").map { user =>
      if(userDAO.checkRole(user.toLong) == "seller")
        Ok(views.html.addproductpage())
      else
        Unauthorized
    }.getOrElse{
      Redirect(routes.HomeController.home())
    }
  }

  def addProduct() = Action(parse.multipartFormData(FileUtils.handleFilePartAsFile)) { implicit request =>
    request.session.get("user").map { user =>
      ProductForm.productForm.bindFromRequest.fold(
        formWithErrors => {
          BadRequest(formWithErrors.errorsAsJson)
        },
        productData => {
          val newProduct = productDAO.create(productData.name, productData.description, productData.price).get
          productDAO.changeOwner(newProduct, user.toLong)
          request.body.file("file").map {
            case FilePart(key, filename, contentType, file) =>
              if(file.length() > 0 && S3FileDetails.isImage(contentType.get)) {
                val productImage = UUID.randomUUID().toString
                val outputImage = "C:/internship-task/"+ productImage +".jpg"
                val newFile = new File(outputImage)
                newFile.createNewFile()
                ImageMagickUtils.resizeImage(file.toPath.toString, 500, outputImage)
                S3FileDetails.changeUserAvatar(productImage, newFile)
                productDAO.changeProductImage(newProduct, productImage)
                Files.deleteIfExists(Paths.get(outputImage))
              }
          }
          Ok(Json.toJson(productData))
        }
      )
    }.getOrElse{
      Redirect(routes.HomeController.home())
    }
  }

  //customer
  def productsListPage() = Action { implicit request =>
    request.session.get("user").map { user =>
      Ok(views.html.productslist(productDAO.getAllProducts, productDAO))
    }.getOrElse{
      Redirect(routes.HomeController.home())
    }
  }

  def buyProduct(productId: Long) = Action { implicit request =>
    request.session.get("user").map { user =>
      productDAO.setIsSold(productId)
      productDAO.changeOwner(productId, user.toLong)
      Ok
    }.getOrElse{
      Redirect(routes.HomeController.home())
    }
  }
}
