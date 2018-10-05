package com.org.controller

import java.util.UUID

import com.google.inject.{Inject, Provider}
import com.twitter.finagle.http.{Request, Status}
import com.twitter.inject.Logging
import com.twitter.util.Future

class ServiceController @Inject()(subject: Provider[Option[UUID]]) extends Controller(subject) with Logging {

  get("/load/stockBarang") { request: Request =>
    val result = ServiceControllerHelper.getDataStokBarang()
    toResponse(Future(result), response, Status.Ok)
  }

  get("/load/dataBarangMasuk") { request: Request =>
    val result = ServiceControllerHelper.getDataBarangMasuk()
    toResponse(Future(result), response, Status.Ok)
  }

  get("/load/dataBarangKeluar") { request: Request =>
    val result = ServiceControllerHelper.getDataBarangKeluar()
    toResponse(Future(result), response, Status.Ok)
  }

  get("/load/laporanNilaiBarang") { request: Request =>
    val result = ServiceControllerHelper.getLaporanNilaiBarang()
    toResponse(Future(result), response, Status.Ok)
  }

  get("/load/laporanPenjualan") { request: Request =>
    val result = ServiceControllerHelper.getLaporanPenjualan()
    toResponse(Future(result), response, Status.Ok)
  }

  get("/export/laporanNilaiBarang") { request: Request =>
    val result = ServiceControllerHelper.exportLaporanNilaiBarang()
    toResponse(Future(result), response, Status.Ok)
  }

  get("/export/laporanPenjualan") { request: Request =>
    val result = ServiceControllerHelper.exportLaporanPenjualan()
    toResponse(Future(result), response, Status.Ok)
  }
}
