package com.org.util

import java.util.UUID

import com.twitter.finagle.postgres.Row

import scala.collection.immutable.ListMap
import scala.collection.mutable
import scala.reflect.runtime.universe._

/**
  * Created by subhan on 1/5/17.
  */
object ReflectionHelpers extends ReflectionHelpers

trait ReflectionHelpers {

  protected val classLoaderMirror = runtimeMirror(getClass.getClassLoader)

  def caseMap[T: TypeTag: reflect.ClassTag](instance: T): collection.Map[String, Any] = {
    val im = classLoaderMirror.reflect(instance)
    val map = mutable.HashMap[String, Any]()
    typeOf[T].members.collect {
      case m: MethodSymbol if m.isCaseAccessor =>
        val name  = StringUtil.toUnderscore(m.name.toString)
        val value = im.reflectMethod(m).apply()
        map.put(name, value)
    }
    map
  }

  /**
    * Encapsulates functionality to reflectively invoke the constructor
    * for a given case class type `T`.
    *
    * @tparam T the type of the case class this factory builds
    */
  class CaseClassFactory[T: TypeTag] {

    val tpe = typeOf[T]
    val classSymbol = tpe.typeSymbol.asClass

    if (!(tpe <:< typeOf[Product] && classSymbol.isCaseClass))
      throw new IllegalArgumentException(
        "CaseClassFactory only applies to case classes!"
      )

    val classMirror = classLoaderMirror reflectClass classSymbol

    val constructorSymbol = tpe.decl(termNames.CONSTRUCTOR)

    val defaultConstructor =
      if (constructorSymbol.isMethod) constructorSymbol.asMethod
      else {
        val ctors = constructorSymbol.asTerm.alternatives
        ctors.map {
          _.asMethod
        }.find {
          _.isPrimaryConstructor
        }.get
      }

    val constructorMethod = classMirror reflectConstructor defaultConstructor

    /**
      * Attempts to create a new instance of the specified type by calling the
      * constructor method with the supplied arguments.
      *
      * @param args the arguments to supply to the constructor method
      */
    def buildWith(args: Vector[_]): T = constructorMethod(args: _*).asInstanceOf[T]

    /**
      * Convert from Row to params
      *
      * @param row
      * @return
      */
    def autoParams(row: Row): Vector[Any] = {
      (Vector[Any]() /: typeOf[T].members.filter(!_.isMethod).toSeq.reverse) { (vec, prop) =>
        val key = prop.name.toString.trim
        vec :+ key

        val value = prop.typeSignature match {
          case x if x == typeOf[String] => row.get[String](key)
          case i if i == typeOf[Int] => row.get[Int](key)
          case l if l == typeOf[Long] => row.get[Long](key)
          case d if d == typeOf[Double] => row.get[Double](key)
          case f if f == typeOf[Float] => row.get[Float](key)
          case f if f == typeOf[UUID] => row.get[UUID](key)
          case o if o == typeOf[Option[String]] => row.getOption[String](key)
          case o if o == typeOf[Option[Int]] => row.getOption[Int](key)
          case o if o == typeOf[Option[Long]] => row.getOption[Long](key)
          case o if o == typeOf[Option[Double]] => row.getOption[Double](key)
          case o if o == typeOf[Option[Float]] => row.getOption[Float](key)
          case o if o == typeOf[Option[UUID]] => row.getOption[UUID](key)
          //todo time, date
        }
        vec :+ value
      }
    }


    /**
      * Supaya reflection hanya dipakai di row pertama, habis itu memakai function parser.
      * Pakai ListMap untuk keep insertion order
      *
      * @param row
      * @return
      */
    def parserMap(row: Row): ListMap[String, (Row) => Any] = {
      (ListMap[String, (Row) => Any]() /: typeOf[T].members.filter(!_.isMethod).toSeq.reverse) { (map, prop) =>
        val key1 = prop.name.toString.trim
        val key2 = StringUtil.toUnderscore(prop.name.toString.trim)
        prop.typeSignature match {
          case x if x == typeOf[String] => map + (key1 -> ((row) => row.get[String](key2)))
          case x if x == typeOf[Int] => map + (key1 -> ((row) => row.get[Int](key2)))
          case x if x == typeOf[Long] => map + (key1 -> ((row) => row.get[Long](key2)))
          case x if x == typeOf[Double] => map + (key1 -> ((row) => row.get[Double](key2)))
          case x if x == typeOf[Float] => map + (key1 -> ((row) => row.get[Float](key2)))
          case x if x.toString == "java.util.UUID" => map + (key1 -> ((row) => row.get[UUID](key2)))
          case x if x == typeOf[Option[String]] => map + (key1 -> ((row) => row.getOption[String](key2)))
          case x if x == typeOf[Option[Int]] => map + (key1 -> ((row) => row.getOption[Int](key2)))
          case x if x == typeOf[Option[Long]] => map + (key1 -> ((row) => row.getOption[Long](key2)))
          case x if x == typeOf[Option[Double]] => map + (key1 -> ((row) => row.getOption[Double](key2)))
          case x if x == typeOf[Option[Float]] => map + (key1 -> ((row) => row.getOption[Float](key2)))
          case x if x == typeOf[Option[UUID]] => map + (key1 -> ((row) => row.getOption[UUID](key2)))
        }
      }
    }


    def parse(row: Row, parsermap: Map[String, (Row) => Any]): T = {
      val vec = (Vector[Any]() /: parsermap) { (vec_, map) =>
        vec_ :+ map._1
        vec_ :+ map._2(row)
      }
      buildWith(vec)
    }
  }




}