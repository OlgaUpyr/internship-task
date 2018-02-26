package models

import java.io.File

import com.typesafe.config.ConfigFactory
import controllers.routes
import utils.AmazonS3ClientWrapper

object S3FileDetails {
  val amazonS3ClientWrapper = new AmazonS3ClientWrapper
  val bucket = ConfigFactory.load().getString("aws.s3.bucket")

  def changeUserAvatar(fileName: String, file: File) = {
    amazonS3ClientWrapper.uploadFile(fileName, bucket, file)
  }

  def isImage(contentType: String): Boolean = {
    if(contentType.equals("image/bmp") || contentType.equals("image/png") || contentType.equals("image/jpg")
      || contentType.equals("image/jpeg") || contentType.equals("image/giff")) true
    else false
  }
}
