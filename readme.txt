cara penggunaan aplikasi
menggunakan OS windows
contoh directory aplikasi
anggap bahwa project ini di ekstrak ke /home/user/project-saya/
-buat folder ke /home/user/project-saya/Upload
-buat folder ke /home/user/project-saya/Database
-buka project dan load build.sbt
-IMPORT spreadsheet Toko Ijah.xlsx ke /home/user/project-saya/Upload
-jalankan aplikasi main/scala/com/org/Server.scala
-tunggu hingga database selesai inisialisasi


setelah selesai ini adalah beberapa service yang disediakan (bisa menggunakan RESTLET CLIENT)
1. http://localhost:8085/load/stockBarang (untuk get data json dari database stock barang)
2. http://localhost:8085/load/dataBarangMasuk (untuk get data json dari database barang masuk)
3. http://localhost:8085/load/dataBarangKeluar (untuk get data json dari database barang keluar)
4. http://localhost:8085/load/laporanNilaiBarang (untuk get data json dari laporan nilai barang)
5. http://localhost:8085/load/laporanPenjualan (untuk get data json dari laporan penjualan)

untuk export laporan Nilai Barang dan Penjualan
1. http://localhost:8085/export/laporanNilaiBarang (hasil export laporan Nilai Barang akan muncul pada /home/user/project-saya/Export)
2. http://localhost:8085/export/laporanPenjualan (hasil export laporan Penjualan akan muncul pada /home/user/project-saya/Export)