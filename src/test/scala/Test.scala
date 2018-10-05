import com.spark.SparkFunc

object Test extends App {
  //linux
  //  System.setProperty("hadoop.home.dir", "/home/dremio")
  //windows
  System.setProperty("hadoop.home.dir", "C:\\hadoop")

  val dirToReadExcel = System.getProperty("user.dir") + "/upload/Toko Ijah.xlsx"
  val dirToWriteCsv = System.getProperty("user.dir") + "/database/"
  val dirFileBarangMasuk = dirToWriteCsv + "BarangMasuk.csv"
  val dirFileBarangKeluar = dirToWriteCsv + "BarangKeluar.csv"

  SparkFunc.processingExcelToCsv(dirToReadExcel, dirToWriteCsv)

  SparkFunc.loadCsvBarangMasukToDB(dirFileBarangMasuk)
  SparkFunc.loadCsvBarangKeluarToDB(dirFileBarangKeluar)

  SparkFunc.queryLaporanNilaiBarang(dirFileBarangMasuk)
  SparkFunc.queryLaporanPenjualan(dirFileBarangKeluar)
}
