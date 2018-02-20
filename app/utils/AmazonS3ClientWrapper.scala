package utils

import java.io.File

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.AmazonS3Exception
import com.typesafe.config.ConfigFactory

class AmazonS3ClientWrapper() {
  private val accessKey = ConfigFactory.load().getString("aws.s3.accesskey")
  private val secretKey = ConfigFactory.load().getString("aws.s3.secretkey")
  private val credentials = new BasicAWSCredentials(accessKey, secretKey)
  private val client = new AmazonS3Client(credentials)

  def uploadFile(key: String, bucket: String, content: File) = {
    try {
      client.putObject(bucket, key, content)
      client.getResourceUrl(bucket, key)
    } catch {
      case e: AmazonS3Exception =>
        Console.println(s"Failed to upload to aws.s3 with error: ${e.getMessage} for file: $key ")
    }
  }

  def deleteFile(bucket: String, key: String) = {
    try {
      client.deleteObject(bucket, key)
    } catch {
      case e: Exception => Console.println("delete file" + e.getMessage)
    }
  }

  def doesFileExist(bucket: String, key: String): Boolean = {
    try {
      client.getObjectMetadata(bucket, key); true
    } catch {
      case e: Exception => Console.println("doesFileExist=" + e.getMessage); false
    }
  }
}
