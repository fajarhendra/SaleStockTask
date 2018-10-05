package com.org.util

/**
  * Created by subhan on 1/4/17.
  */
object StringUtil {

  /*
  * Takes an underscore separated identifier name and returns a camel cased one
  *
  * Example:
  *    toCamel("this_is_a_1_test") == "thisIsA1Test"
  */
  def toCamel(s: String): String = {
    val split = s.split("_")
    val tail = split.tail.map { x => x.head.toUpper + x.tail }
    split.head + tail.mkString
  }

  /**
    * Takes a camel cased identifier name and returns an underscore separated
    * name
    *
    * Example:
    * toUnderscore("thisIsA1Test") == "this_is_a_1_test"
    */
  def toUnderscore(text: String) = text.drop(1).foldLeft(text.headOption.map(_.toLower + "") getOrElse "") {
    case (acc, c) if c.isUpper => acc + "_" + c.toLower
    case (acc, c) => acc + c
  }

  /**
    * nambah tanda perik 1
    *
    * @param x
    * @return
    */
  def stringify(x: Any): Any = {
    if (x.isInstanceOf[String])
      s"'${x}'"
    else
      x
  }


  /**
    * nambah tanda ""
    *
    * @param x
    * @return
    */
  def stringify2(x: Any): Any = {
    if (x.isInstanceOf[String])
      s""""${x}""""
    else
      x
  }


  /**
    * Stringify jika input ada kemungkinan berupa option
    * @param x
    * @return
    */
  def stringifyOption(x: Any): Any = {
    x match {
      case Some(y) => stringify(y)
      case None => "null"
      case  z: Any => stringify(z)
      case _ => "null"
    }
  }

}
