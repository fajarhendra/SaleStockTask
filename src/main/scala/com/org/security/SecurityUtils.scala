package com.org.security

import java.nio.charset.Charset
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import com.google.common.base.Charsets.UTF_8
import com.org.controller.Credentials
import org.apache.commons.codec.binary.Base64.{decodeBase64, encodeBase64}

object SecurityUtils {
  var HmacSHA1 = "HmacSHA1"
  val Secret = "=7dE=cui192"

  def generateHmac(data: String): String = {
    val signingKey = new SecretKeySpec(Secret.getBytes(), HmacSHA1)
    val mac = Mac.getInstance(HmacSHA1)
    mac.init(signingKey)

    val rawHmac = mac.doFinal(data.getBytes(UTF_8))
    toHex(rawHmac)
  }

  private def toHex(in: Array[Byte]): String = in.map("%02x".format(_)).mkString

  def credentialsFromAuthHeader(authHeader: String): Option[Credentials] = {
    if (!authHeader.contains("Basic ")) None
    else {
      val baStr = authHeader.replaceFirst("Basic ", "")
      val decoded = Base64.decode(baStr)
      if (!decoded.contains(":")) None
      else {
        val Array(user, password) = decoded.split(":")
        Some(Credentials(user, password))
      }
    }
  }

  def authHeaderFromCredentials(credentials: Credentials): String = {
    "Basic " + Base64.encode(s"${credentials.username}:${credentials.password}")
  }

  object Base64 {
    def decode(encoded: String) = new String(decodeBase64(encoded.getBytes))

    def encode(decoded: String) = new String(encodeBase64(decoded.getBytes))
  }

}
