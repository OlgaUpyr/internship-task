package utils

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

import models.User

import java.util.Random

object PasswordUtils {
  def generateRandomString(length: Int): String = {
    val sb = new StringBuffer()
    val r = new Random()
    for (i <- 1 to length)
      sb.append((r.nextInt(25) + 65).toChar)
    sb.toString
  }

  private val hexArray = "0123456789ABCDEF".toCharArray
  def toHex(bytes: Array[Byte]): String = {
    val hexChars = new Array[Char](bytes.length * 2)
    for (j <- bytes.indices) {
      val v = bytes(j) & 0xFF
      hexChars(j * 2) = hexArray(v >>> 4)
      hexChars(j * 2 + 1) = hexArray(v & 0x0F)
    }
    new String(hexChars)
  }

  def isPasswordsEquals(a: String, b: String): Boolean = {
    if (a.length != b.length) {
      false
    }
    else {
      var equal = 0
      for (i <- Array.range(0, a.length)) {
        equal |= a(i) ^ b(i)
      }
      equal == 0
    }
  }

  def encryptPassword(password: String, salt: String): String = {
    val keySpec = new PBEKeySpec(password.toCharArray, salt.getBytes, 10000, 128)
    val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    val bytes = secretKeyFactory.generateSecret(keySpec).getEncoded
    toHex(bytes)
  }

  def passwordsMatch(password: String, user: User): Boolean = {
    isPasswordsEquals(
      user.newPassword,
      encryptPassword(password, user.salt.get)
    )
  }
}