package com.org.controller

import com.spark.SparkFunc
import com.sun.javaws.Launcher
import com.serverRun
import com.serverRun.LauncherSpark
import org.apache.spark.sql.SaveMode

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object ServiceControllerHelper {

  def getDataStokBarang(): ListBuffer[mutable.HashMap[String, Any]] = {
    val result = new ListBuffer[mutable.HashMap[String, Any]]
    val db = SparkFunc.getDbStockBarang()
    val df = db.collect()
    df.foreach(row => {
      val tmpRes = new mutable.HashMap[String, Any]
      tmpRes.put("SKU", row(0))
      tmpRes.put("Nama_Item", row(1))
      tmpRes.put("Jumlah_Sekarang", row(2))
      result += tmpRes
    })
    result
  }

  def getDataBarangMasuk(): ListBuffer[mutable.HashMap[String, Any]] = {
    val result = new ListBuffer[mutable.HashMap[String, Any]]
    val db = SparkFunc.getDbBarangMasuk()
    val df = db.collect()
    df.foreach(row => {
      val tmpRes = new mutable.HashMap[String, Any]
      tmpRes.put("Waktu", row(0))
      tmpRes.put("SKU_Barang_Masuk", row(1))
      tmpRes.put("Nama_Barang", row(2))
      tmpRes.put("Jumlah_Pemesanan", row(3))
      tmpRes.put("Jumlah_Diterima", row(4))
      tmpRes.put("Harga_Beli", row(5))
      tmpRes.put("Total", row(6))
      tmpRes.put("Nomer_Kwitansi", row(7))
      tmpRes.put("Catatan", row(8))
      result += tmpRes
    })
    result
  }

  def getDataBarangKeluar(): ListBuffer[mutable.HashMap[String, Any]] = {
    val result = new ListBuffer[mutable.HashMap[String, Any]]
    val db = SparkFunc.getDbBarangKeluar()
    val df = db.collect()
    df.foreach(row => {
      val tmpRes = new mutable.HashMap[String, Any]
      tmpRes.put("Waktu", row(0))
      tmpRes.put("SKU_Barang_Keluar", row(1))
      tmpRes.put("Nama_Barang", row(2))
      tmpRes.put("Jumlah_Keluar", row(3))
      tmpRes.put("Harga_Jual", row(4))
      tmpRes.put("Total", row(5))
      tmpRes.put("ID_Pesanan", row(6))
      result += tmpRes
    })
    result
  }

  def getLaporanNilaiBarang(): ListBuffer[mutable.HashMap[String, Any]] = {
    val result = new ListBuffer[mutable.HashMap[String, Any]]
    val db = SparkFunc.getLaporanNilaiBarang()
    val df = db.collect()
    df.foreach(row => {
      val tmpRes = new mutable.HashMap[String, Any]
      tmpRes.put("SKU_Barang_Masuk", row(0))
      tmpRes.put("Nama_Barang", row(1))
      tmpRes.put("JUMLAH", row(2))
      tmpRes.put("Rata-Rata_Harga_Beli(Rp)", row(3))
      tmpRes.put("TOTAL(Rp)", row(4))
      result += tmpRes
    })
    result
  }

  def getLaporanPenjualan(): ListBuffer[mutable.HashMap[String, Any]] = {
    val result = new ListBuffer[mutable.HashMap[String, Any]]
    val db = SparkFunc.getLaporanPenjualan()
    val df = db.collect()
    df.foreach(row => {
      val tmpRes = new mutable.HashMap[String, Any]
      tmpRes.put("ID_Pesanan", row(0))
      tmpRes.put("Waktu", row(1))
      tmpRes.put("SKU", row(2))
      tmpRes.put("Nama_Barang", row(3))
      tmpRes.put("Jumlah", row(4))
      tmpRes.put("Harga_Jual", row(5))
      tmpRes.put("Total", row(6))
      tmpRes.put("Harga_Beli", row(7))
      tmpRes.put("Laba(Rp)", row(8))
      result += tmpRes
    })
    result
  }

  def exportLaporanNilaiBarang():Unit = {
    SparkFunc.exportLaporanNilaiBarang()
  }
  def exportLaporanPenjualan():Unit = {
    SparkFunc.exportLaporanPenjualan()
  }
}
