package com.org.util

import java.net.InetAddress
import java.time.{Instant, LocalDate, LocalDateTime}
import java.util.UUID

import com.org.service.DB
import com.twitter.finagle.postgres.values.Types
import com.twitter.finagle.postgres.{OK, Row, RowImpl}
import com.twitter.util.Future

import scala.collection.immutable.Map

/**
  * Created by subhan on 1/3/17.
  */
object PostgresUtil {


  /**
    * Konversi secara otomatis dari row menjadi tipe data yang sesuai
    *
    * String - for text, char, varchar, citext, and other stringy Postgres values (including ENUM)
    * Byte, Short, Int, Long - for the Postgres integers of their corresponding widths
    * Float, Double - for the Postgres floating-point values of their corresponding precision
    * BigDecimal, java.math.BigDecimal - for Postgres numeric values of arbitrary precision
    * java.util.UUID - for Postgres uuid values
    * java.time.LocalDate - for Postgres date values
    * java.time.LocalDateTime - for Postgres timestamp without time zone values
    * java.time.Instant, java.time.OffsetDateTime, java.time.ZonedDateTime - for Postgres timestamp with time zone values
    * java.net.InetAddress - for Postgres inet values
    * Map[String, Option[String]] - for Postgres hstore values
    *
    * @param row
    * @param name nama kolom
    * @return
    */
  def autoType(row: Row, name: String): Any = {
    val format = row.asInstanceOf[RowImpl].rowFormat
    val index = format.indexMap.get(name).getOrElse(0)
    val typeCode = format.oids(index)
    typeConvert(name, typeCode, row)
  }


  private def typeConvert(name: String, typeCode: Int, row: Row): Any = {
    typeCode match {
      case Types.VAR_CHAR => row.get[String](name)
      case Types.INT_4 => row.get[Int](name)
      case Types.INT_8 => row.get[Long](name)
        //todo implicit value ke byte type
//      case Types.INT_2 => row.get[Byte](name)
      case Types.FLOAT_8 => row.get[Double](name)
      case Types.BOOL => row.get[Boolean](name)
      case Types.NUMERIC => row.get[BigDecimal](name)
      case Types.TEXT => row.get[String](name)
      //todo implicit value ke char type
//      case Types.CHAR => row.get[Char](name)
      case Types.UUID => row.get[UUID](name)
      case Types.DATE => row.get[LocalDate](name)
      case Types.TIME => row.get[LocalDateTime](name)
      case Types.TIMESTAMP => row.get[Instant](name)
      case Types.INET => row.get[InetAddress](name)
      case _ => row.getAnyOption(name)
    }
  }


  def parseMap(row: Row): Map[String, (Row) => Any] = {
    val format = row.asInstanceOf[RowImpl].rowFormat
    (Map[String, (Row) => Any]() /: format.indexMap) { (map, tuple) =>
      val name = tuple._1
      val typeCode = format.oids(tuple._2)

      typeCode match {
        case Types.VAR_CHAR => map +(name, ((row) => row.get[String](name)))
        case Types.INT_4 => map +(name, ((row) => row.get[Int](name)))
        case Types.INT_8 => map +(name, ((row) => row.get[Long](name)))

        //todo implicit value ke byte type
//        case Types.INT_2 => map +(name, ((row) => row.get[Byte](name)))
        case Types.FLOAT_8 => map +(name, ((row) => row.get[Double](name)))
        case Types.BOOL => map +(name, ((row) => row.get[Boolean](name)))
        case Types.NUMERIC => map +(name, ((row) => row.get[BigDecimal](name)))
        case Types.TEXT => map +(name, ((row) => row.get[String](name)))
        //todo implicit value ke char type
//        case Types.CHAR => map +(name, ((row) => row.get[Char](name)))
        case Types.UUID => map +(name, ((row) => row.get[UUID](name)))
        case Types.DATE => map +(name, ((row) => row.get[LocalDate](name)))
        case Types.TIME => map +(name, ((row) => row.get[LocalDateTime](name)))
        case Types.TIMESTAMP => map +(name, ((row) => row.get[Instant](name)))
        case Types.INET => map +(name, ((row) => row.get[InetAddress](name)))
        case _ => map +(name, ((row) => row.getAnyOption(name)))
      }
    }
  }


  /**
    * Query select untuk SQL bebas
    *
    * @param sql misal "select * from table_name where id=1"
    * @return
    */
  def list(sql: String): Future[Seq[Map[String, Any]]] = {
    var parserMap = Map[String, (Row) => Any]()
    DB.client.select(sql) { row =>
      if (parserMap.isEmpty)
        parserMap = parseMap(row)

      (Map[String, Any]() /: parserMap) { (map, fungsi) =>
        val name = fungsi._1
        val func = fungsi._2(row)
        map + (name -> func)
      }
    }
  }


  /**
    * untuk menyusun phrase dalam sql, misal where dan set di syntax update.
    * Contoh phrase("where", "AND", Map("id" -> 1)
    *
    * @param start misal set atau where
    * @param end   misal koma atau END
    * @param map
    * @return
    */
  private def phrase(start: String, end: String, map: collection.Map[String, Any]): String = {
    var result = s"${start} "
    for ((k, v) <- map) {
      if (v == null)
        result = s"${result} ${k} IS NULL ${end} "
      else {
        val vString = StringUtil.stringify(v)
        result = s"${result} ${k} = ${vString} ${end} "
      }
    }
    result.substring(0, result.length - end.length - 1)
  }


  /**
    * List all semua baris dari 1 table
    *
    * @param table nama table
    * @return
    */
  def listAll(table: String): Future[Seq[Map[String, Any]]] = {
    val sql = s"select * from ${table}"
    list(sql)
  }

  /**
    * select dengan operator AND, dan semua operator clause-nya "=" atau "equal"
    *
    * @param map misal Map("id" -> 1)
    * @return
    */
  def listAnd(table: String, map: collection.Map[String, Any]): Future[Seq[Map[String, Any]]] = {
    val frase = phrase("where", "AND", map)
    val sql = s"select * from ${table} ${frase}"
    list(sql)
  }

  /**
    * select dengan operator OR, dan semua operator clause-nya "=" atau "equal"
    *
    * @param map misal Map("id" -> 1)
    * @return
    */
  def listOr(table: String, map: collection.Map[String, Any]): Future[Seq[Map[String, Any]]] = {
    val frase = phrase("where", "OR", map)
    val sql = s"select * from ${table} ${frase}"
    list(sql)
  }


  /**
    * frase where pada select, dengan operator-nya
    * misal phraseTuple("where", "AND", ("id", ">", 1))
    *
    * @param start
    * @param end
    * @param tuples misal ("id", "=", 1)
    * @return
    */
  def phraseTuple(start: String, end: String, tuples: Seq[(String, String, Any)]): String = {
    var result = s"${start} "
    tuples.map { tuple =>
      val value = if (tuple._3 == "NULL") "NULL" else StringUtil.stringify(tuple._3)
      result = s"${result} ${tuple._1} ${tuple._2} ${value} ${end} "
    }
    result.substring(0, result.length - end.length - 1)
  }


  /**
    * select dengan operator AND dan operator clause-nya bisa detail
    *
    * @param tuples misal ("id", ">", 1)
    * @return
    */
  def selectAnd(table: String, tuples: Seq[(String, String, Any)]): Future[Seq[Map[String, Any]]] = {
    val frase = phraseTuple("where", "AND", tuples)
    val sql = s"select * from ${table} ${frase}"
    list(sql)
  }

  /**
    * select dengan operator OR dan operator clause-nya bisa detail
    *
    * @param tuples misal ("id", "=", 1)
    * @return
    */
  def selectOr(table: String, tuples: Seq[(String, String, Any)]): Future[Seq[Map[String, Any]]] = {
    val frase = phraseTuple("where", "OR", tuples)
    val sql = s"select * from ${table} ${frase}"
    list(sql)
  }

  def insert(table: String, keyVal: Map[String, Any]): Future[OK] = {
    val columns = keyVal.keySet.mkString("(", ", ", ")")
    val values = keyVal.values.map(x => StringUtil.stringify(x)).mkString("(", ", ", ")")
    val sql = s"insert into ${table} ${columns} values ${values}"
    DB.client.execute(sql)
  }

  def insertBatch(table: String, rows: Seq[Map[String, Any]]): Future[OK] = {
    var sql = ""
    rows.map { keyVal =>
      val columns = keyVal.keySet.mkString("(", ", ", ")")
      val values = keyVal.values.map(x => StringUtil.stringify(x)).mkString("(", ", ", ")")
      sql = s"${sql}insert into ${table} ${columns} values ${values};\n"
    }
    DB.client.execute(sql)
  }

}
