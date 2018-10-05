package com.org.security

import com.github.t3hnar.bcrypt._
import com.twitter.util.Try
import org.jasypt.util.text.BasicTextEncryptor

trait Crypto {
  def encrypt(data: String): String

  def decrypt(data: String): Option[String]
}

trait PasswordCrypto {
  def encryptPassword(password: String): String

  def checkPassword(password: String, underTest: String): Boolean
}

class BasicCrypto(secret: String) extends Crypto {

  val crypto = new BasicTextEncryptor
  crypto.setPassword(secret)

  override def decrypt(data: String): Option[String] = {
    Try(crypto.decrypt(data)).toOption
  }

  override def encrypt(data: String): String = {
    crypto.encrypt(data)
  }
}

class BCryptPasswordCrypto extends PasswordCrypto {

  override def encryptPassword(password: String): String = {
    password.bcrypt(generateSalt)
  }

  override def checkPassword(password: String, underTest: String): Boolean = {
    underTest.isBcrypted(password)
  }
}