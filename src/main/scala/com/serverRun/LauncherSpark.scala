package com.serverRun

import com.spark.SparkFunc

object LauncherSpark {
  def run(): Unit = {
      println("PROCESS DATABASE")
      //linux
      //  System.setProperty("hadoop.home.dir", "/home/dremio")
      //windows
      System.setProperty("hadoop.home.dir", "C:\\hadoop")

      val dirToReadExcel = System.getProperty("user.dir") + "/Upload/Toko Ijah.xlsx"
      val dirToWriteCsv = System.getProperty("user.dir") + "/Database/"
      val dirFileStockBarang = dirToWriteCsv + "StockBarang.csv"
      val dirFileBarangMasuk = dirToWriteCsv + "BarangMasuk.csv"
      val dirFileBarangKeluar = dirToWriteCsv + "BarangKeluar.csv"

      SparkFunc.processingExcelToCsv(dirToReadExcel, dirToWriteCsv)

      SparkFunc.loadCsvStockBarangToDB(dirFileStockBarang)
      SparkFunc.loadCsvBarangMasukToDB(dirFileBarangMasuk)
      SparkFunc.loadCsvBarangKeluarToDB(dirFileBarangKeluar)
      SparkFunc.queryLaporanNilaiBarang(dirFileBarangMasuk)
      SparkFunc.queryLaporanPenjualan(dirFileBarangKeluar)
      println("DONE !!")
  }
}
