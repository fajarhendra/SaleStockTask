package com.spark


import java.io.FileWriter

import com.twitter.server.EventSink.Configuration
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.{SparkConf, SparkContext, sql}
import org.apache.spark.sql.SQLContext

import scala.collection.mutable.ListBuffer

object SparkFunc {
  private val dbStockBarang: sql.DataFrame = null
  private val dbBarangMasuk: sql.DataFrame = null
  private val dbBarangKeluar: sql.DataFrame = null

  def getDbStockBarang(): sql.DataFrame = {
    dbStockBarang
  }

  def getDbBarangMasuk(): sql.DataFrame = {
    dbBarangMasuk
  }

  def getDbBarangKeluar(): sql.DataFrame = {
    dbBarangKeluar
  }

  def loadExcelStockBarang(dirToRead: String): sql.DataFrame = {
    val df = ConfigSpark.session.spark.read
      .format("com.crealytics.spark.excel")
      .option("useHeader", "true")
      .option("sheetName", "Catatan Jumlah Barang")
      .load(dirToRead)
    df
  }

  def loadExcelBarangMasuk(dirToRead: String): sql.DataFrame = {
    val df = ConfigSpark.session.spark.read
      .format("com.crealytics.spark.excel")
      .option("useHeader", "true")
      .option("sheetName", "Catatan Barang Masuk")
      .load(dirToRead)
    df
  }

  def loadExcelBarangKeluar(dirToRead: String): sql.DataFrame = {
    val df = ConfigSpark.session.spark.read
      .format("com.crealytics.spark.excel")
      .option("useHeader", "true")
      .option("sheetName", "Catatan Barang Keluar")
      .load(dirToRead)
    df
  }

  def excelToCsv(df: sql.DataFrame, dirToWrite: String): Unit = {
    val rowData = new ListBuffer[String]
    if (dirToWrite.contains("StockBarang")){
      rowData.append("SKU,Nama_Item,Jumlah_Sekarang\n")
    }else if (dirToWrite.contains("BarangMasuk")){
      rowData.append("Waktu,SKU,Nama_Barang,Jumlah_Pemesanan,Jumlah_Diterima,Harga_Beli,Total,Nomer_Kwitansi,Catatan\n")
    }else if (dirToWrite.contains("BarangKeluar")){
      rowData.append("Waktu,SKU,Nama_Barang,Jumlah_Keluar,Harga_Jual,Total,Catatan\n")
    }

    val dfDB = df.collect() // header tidak terambil
    dfDB.foreach(row => {
      if (row(0) != null) {
        val tempRowData = new ListBuffer[String]
        for (i <- 0 to row.size - 1) {
          tempRowData.append(row(i).toString.replace("[$]", ""))
        }
        rowData.append(tempRowData.mkString(",") + "\n")
      }
    })

    //list to csv
    val csvWritter = new FileWriter(dirToWrite)
    rowData.foreach(row => {
      csvWritter.append(row)
    })
    csvWritter.close()
  }

  def processingExcelToCsv(dirToReadExcel: String, dirToWriteCsv: String): Unit = {
    val dfStockBarang = loadExcelStockBarang(dirToReadExcel)
    val dfBarangMasuk = loadExcelBarangMasuk(dirToReadExcel)
    val dfBarangKeluar = loadExcelBarangKeluar(dirToReadExcel)
    excelToCsv(dfStockBarang, dirToWriteCsv + "StockBarang.csv")
    excelToCsv(dfBarangMasuk, dirToWriteCsv + "BarangMasuk.csv")
    excelToCsv(dfBarangKeluar.drop("_c7"), dirToWriteCsv + "BarangKeluar.csv")
  }
}
