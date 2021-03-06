package com.spark


import java.io.FileWriter

import com.twitter.server.EventSink.Configuration
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.{SparkConf, SparkContext, sql}
import org.apache.spark.sql.SQLContext

import scala.collection.mutable.ListBuffer

object SparkFunc {
  var dbStockBarang: sql.DataFrame = null
  var dbBarangMasuk: sql.DataFrame = null
  var dbBarangKeluar: sql.DataFrame = null
  var laporanNilaiBarang: sql.DataFrame = null
  var laporanPenjualan: sql.DataFrame = null

  def getDbStockBarang(): sql.DataFrame = {
    dbStockBarang
  }

  def getDbBarangMasuk(): sql.DataFrame = {
    dbBarangMasuk
  }

  def getDbBarangKeluar(): sql.DataFrame = {
    dbBarangKeluar
  }

  def getLaporanNilaiBarang(): sql.DataFrame = {
    laporanNilaiBarang
  }

  def getLaporanPenjualan(): sql.DataFrame = {
    laporanPenjualan
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

  def loadCsvStockBarangToDB(dirToRead: String): sql.DataFrame = {
    val df = ConfigSpark.session.spark.read
      .option("header", true)
      .option("delimiter", ",")
      .option("maxColumns", 15)
      .csv(dirToRead)
    dbStockBarang = df
    dbStockBarang
  }

  def loadCsvBarangMasukToDB(dirToRead: String): sql.DataFrame = {
    val df = ConfigSpark.session.spark.read
      .option("header", true)
      .option("delimiter", ",")
      .option("maxColumns", 15)
      .csv(dirToRead)
    dbBarangMasuk = df
    dbBarangMasuk
  }

  def loadCsvBarangKeluarToDB(dirToRead: String): sql.DataFrame = {
    val df = ConfigSpark.session.spark.read
      .option("header", true)
      .option("delimiter", ",")
      .option("maxColumns", 15)
      .csv(dirToRead)
    dbBarangKeluar = df
    dbBarangKeluar
  }


  def excelToCsv(df: sql.DataFrame, dirToWrite: String): Unit = {
    val rowData = new ListBuffer[String]
    if (dirToWrite.contains("StockBarang")) {
      rowData.append("SKU,Nama_Item,Jumlah_Sekarang\n")
    } else if (dirToWrite.contains("BarangMasuk")) {
      rowData.append("Waktu,SKU_Barang_Masuk,Nama_Barang,Jumlah_Pemesanan,Jumlah_Diterima,Harga_Beli,Total,Nomer_Kwitansi,Catatan\n")
    } else if (dirToWrite.contains("BarangKeluar")) {
      rowData.append("Waktu,SKU_Barang_Keluar,Nama_Barang,Jumlah_Keluar,Harga_Jual,Total,ID_Pesanan\n")
    }

    val dfDB = df.collect() // header tidak terambil
    dfDB.foreach(row => {
      if (row(0) != null) {
        val tempRowData = new ListBuffer[String]
        for (i <- 0 to row.size - 1) {
          if (i == 2) {
            tempRowData.append(row(i).toString.replace(",", "_"))
          }
          else if (i == 6) {
            tempRowData.append(row(i).toString.replace("Pesanan ", ""))
          }
          else {
            tempRowData.append(row(i).toString.replace("[$Rp]", "") replace(",", "."))
          }
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

  def queryLaporanNilaiBarang(dirFile: String): Unit = {
    val df = dbBarangMasuk
    df.createOrReplaceTempView("barang_masuk")
    val result = df.sqlContext.sql("""SELECT `SKU_Barang_Masuk`,`Nama_Barang`,SUM(`Jumlah_Diterima`) AS `JUMLAH`,SUM(`Harga_Beli`)/COUNT(`Harga_Beli`)*1000 AS `Rata-Rata_Harga_Beli(Rp)`, SUM(`Jumlah_Diterima`)*SUM(`Harga_Beli`)/COUNT(`Harga_Beli`)*1000 AS `TOTAL(Rp)` FROM barang_masuk GROUP BY `SKU_Barang_Masuk`,`Nama_Barang` """)
    laporanNilaiBarang = result
  }

  def queryLaporanPenjualan(dirFile: String): Unit = {
    val dfPenjualan = dbBarangKeluar
    val dfBarangMasuk = dbBarangMasuk
    dfBarangMasuk.createOrReplaceTempView("barang_masuk")
    val dfBarangMsk = dfBarangMasuk.sqlContext.sql("""SELECT `SKU_Barang_Masuk`,`Harga_Beli` FROM barang_masuk """)
    val dfJoin = dfPenjualan.join(dfBarangMsk, dfPenjualan("SKU_Barang_Keluar") === dfBarangMasuk("SKU_Barang_Masuk")).drop("Total")
    dfJoin.createOrReplaceTempView("penjualan")
    val result = dfJoin.sqlContext.sql("""SELECT `ID_Pesanan`,`Waktu`,`SKU_Barang_Keluar` as `SKU`, `Nama_Barang`,`Jumlah_Keluar` as `Jumlah`, `Harga_Jual`, (`Jumlah_Keluar`*`Harga_Jual`)*1000 as `Total`, `Harga_Beli`, (`Jumlah_Keluar`*`Harga_Jual`)-(`Jumlah_Keluar`*`Harga_Beli`) AS `Laba(Rp)` FROM penjualan GROUP BY `ID_Pesanan`,`Waktu`,`SKU_Barang_Keluar`, `Nama_Barang`,`Jumlah_Keluar`, `Harga_Jual`, `Harga_Beli` """)
    laporanPenjualan = result
  }


  def exportLaporanNilaiBarang(): Unit = {
    val result = new ListBuffer[String]
    val db = getLaporanNilaiBarang()
    val df = db.collect()
    result.append("SKU_Barang_Masuk,Nama_Barang,JUMLAH,Rata-Rata_Harga_Beli(Rp),TOTAL(Rp)"+"\n")
    df.foreach(row => {
      val buildRow = new ListBuffer[String]
      for (i <- 0 to row.size - 1) {
        buildRow.append(row(i).toString)
      }
      result.append(buildRow.mkString(",") + "\n")
    })
    //list to csv
    val csvWritter = new FileWriter(System.getProperty("user.dir") + "/Export/LaporanNilaiBarang.csv")
    result.foreach(row => {
      csvWritter.append(row)
    })
    csvWritter.close()
  }

  def exportLaporanPenjualan(): Unit = {
    val result = new ListBuffer[String]
    val db = getLaporanPenjualan()
    val df = db.collect()
    result.append("ID_Pesanan,Waktu,SKU,Nama_Barang,Jumlah,Harga_Jual,Total,Harga_Beli,Laba(Rp)"+"\n")
    df.foreach(row => {
      val buildRow = new ListBuffer[String]
      for (i <- 0 to row.size - 1) {
        buildRow.append(row(i).toString)
      }
      result.append(buildRow.mkString(",") + "\n")
    })
    //list to csv
    val csvWritter = new FileWriter(System.getProperty("user.dir") + "/Export/LaporanPenjualan.csv")
    result.foreach(row => {
      csvWritter.append(row)
    })
    csvWritter.close()
  }
}
