package models

import java.io.File

import com.typesafe.config.ConfigFactory
import controllers.routes
import utils.AmazonS3ClientWrapper

object S3FileDetails {
  val amazonS3ClientWrapper = new AmazonS3ClientWrapper
  private val bucket = ConfigFactory.load().getString("aws.s3.bucket")

  def getUserAvatar(id: Long) = {
    if(amazonS3ClientWrapper.doesFileExist(bucket, id.toString))
      "https://s3.amazonaws.com/" + bucket + "/" + id
    else
      routes.Assets.versioned("images/user-default.png")
  }

  def changeUserAvatar(id: Long, file: File) = {
    if(amazonS3ClientWrapper.doesFileExist(bucket, id.toString)){
      amazonS3ClientWrapper.deleteFile(bucket, id.toString)
      amazonS3ClientWrapper.uploadFile(id.toString, bucket, file)
    }
    else{
      amazonS3ClientWrapper.uploadFile(id.toString, bucket, file)
    }
  }
}
