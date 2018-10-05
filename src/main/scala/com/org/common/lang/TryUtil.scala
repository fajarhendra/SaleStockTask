package com.org.common.lang

import scala.util.{Try, Failure, Success}

object TryUtil {
  def flattenTry[T](t: Try[Option[T]]): Try[T] = {
    t.flatMap {
      case Some(i) => Success(i)
      case None => Failure(new Exception("Failed to flatten"))
    }
  }
}
