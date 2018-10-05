package com.org.util

import com.org.service.DB
import com.twitter.finagle.postgres.{OK, Row}
import com.twitter.util.Future
import org.apache.poi.ss.formula.functions.T

import scala.collection.mutable
import scala.reflect.runtime.universe._

/**
  * Created by subhan on 1/7/17.
  */
object QueryUtil {


  /**
    * Query dynamic untuk multiple rows
    * @tparam T
    * @return
    */
  def list[T: TypeTag](): Future[Seq[T]] = {
    val table = StringUtil.toUnderscore(typeOf[T].typeSymbol.asClass.toString.substring(6))
    var parseMap = Map[String, (Row) => Any]()
    DB.client.select(s"select * from ${table}") { row: Row =>
      val cellFactory = new ReflectionHelpers.CaseClassFactory[T]
      if (parseMap.isEmpty)
        parseMap = cellFactory.parserMap(row)
      cellFactory.parse(row, parseMap)
    }
  }



  /**
    * mekanisme dari seq menjadi cuma 1 hasil dihandle di user
 *
    * @tparam T
    * @return
    */
  def get[T: TypeTag](sql: String): Future[Seq[T]] = {
    DB.client.select(sql) { row: Row =>
      val cellFactory = new ReflectionHelpers.CaseClassFactory[T]
      val params = cellFactory.autoParams(row)
      cellFactory.buildWith(params)
    }
  }


  /**
    *
    * @param tuples ("id", ">", 1)
    * @tparam T
    * @return
    */
  def listBy[T: TypeTag](tuples: Seq[(String, String, Any)]): Future[Seq[T]] = {
    val table = StringUtil.toUnderscore(typeOf[T].typeSymbol.asClass.toString.substring(6))
    val phrase = PostgresUtil.phraseTuple("where", "AND", tuples)
    val sql = s"select * from ${table} ${phrase}"
    var parseMap = Map[String, (Row) => Any]()
    DB.client.prepareAndQuery(sql) { row: Row =>
      val cellFactory = new ReflectionHelpers.CaseClassFactory[T]
      if (parseMap.isEmpty)
        parseMap = cellFactory.parserMap(row)
      cellFactory.parse(row, parseMap)
    }
  }


  def manualGet[T: TypeTag](sql: String): Future[Seq[T]] = {
    var parseMap = Map[String, (Row) => Any]()
    DB.client.select(sql) { row: Row =>
      val cellFactory = new ReflectionHelpers.CaseClassFactory[T]
      if (parseMap.isEmpty)
        parseMap = cellFactory.parserMap(row)
      cellFactory.parse(row, parseMap)
    }
  }

  /**
    * Tidak di-disain untuk batch. Untuk batch sebagaiknya memakai PostgresUtil.insertBatch
    * @param obj
    * @return
    */
  def insert[T: TypeTag: reflect.ClassTag](obj: T): Future[OK] = {
    val table = StringUtil.toUnderscore(obj.getClass.getSimpleName)
    val convertMap = ReflectionHelpers.caseMap[T](obj)
    println(s"convertMap -> $convertMap")
    val columns = convertMap.keySet.mkString("(", ", ", ")")
    val values = convertMap.values.map(x => StringUtil.stringify(x)).mkString("(", ", ", ")")
    val sql = s"insert into ${table} ${columns} values ${values}"
    println(s"sql -> ${sql}")
    DB.client.execute(sql)
  }


  /**
    *
    * @param obj
    * @param param
    * @param value
    * @return
    */
  def update[T: TypeTag: reflect.ClassTag](obj: T, param: String, value: Any): Future[OK] = {
    val table = StringUtil.toUnderscore(obj.getClass.getSimpleName)
    val convertMap = ReflectionHelpers.caseMap[T](obj)
    val columns = convertMap.keySet.mkString("(", ", ", ")")
    val values = convertMap.values.map(x => StringUtil.stringifyOption(x)).mkString("(", ", ", ")")
    var phrase = ""
    if(param != None || value != None)
      phrase = "where " + param + " = '" + value + "'"
    val sql = s"update ${table} set ${columns} = ${values} ${phrase}"
    println(s"sql -> ${sql}")
    DB.client.execute(sql)
  }




}
