package utils

import java.util.UUID
import java.util.concurrent.TimeUnit

class TimeTokenUtils() {
  def getCurrentTime(): Long = {
    System.currentTimeMillis()
  }

  def getExpirationTime(): Long = {
    getCurrentTime() + TimeUnit.MINUTES.toMillis(10)
  }

  def getToken(): String = {
    UUID.randomUUID().toString
  }
}
