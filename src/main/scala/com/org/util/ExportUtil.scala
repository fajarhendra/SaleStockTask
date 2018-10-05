package com.org.util

import java.util.{Locale, Currency}
import java.text.NumberFormat

object ExportUtil {

  def formatNumber(value: Double): String = {
    val number = NumberFormat.getNumberInstance(new Locale("us", "US"))
    number.format(value)
  }

  def formatPercentage(value: Double): String = {
    val percent = NumberFormat.getPercentInstance(new Locale("id", "ID"))
    percent.setMinimumFractionDigits(2)
    percent.format(value)
  }


  def formatCurrency(value: Double): String = {
    val id = Currency.getInstance(new Locale("id", "ID"))
    val currency = NumberFormat.getCurrencyInstance
    currency.setCurrency(id)
    currency.format(value)
  }


}