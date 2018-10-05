package com.spark

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession

object ConfigSpark {
  //linux
  //  System.setProperty("hadoop.home.dir", "/home/dremio")

  //windows
  System.setProperty("hadoop.home.dir", "C:\\hadoop")
  object session {
    //    System.setProperty("hadoop.home.dir","C:\\hadoop" ) // <<< windows
    //  System.setProperty("hadoop.home.dir", "/home") // <<< linux
    val spark = SparkSession
      .builder()
      .master("local")
      .appName("invent")
      .getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")
  }

  //
  object SparkConfig {
    val conf = new SparkConf()
      .setMaster("local[2]")
      .setAppName("invent")
      .set("spark.executor.memory", "1g")
      .set("spark.driver.memory", "1g")
      .set("spark.some.config.option", "some-value")
    //      .set("spark.testing.memory", "4147480000")
    val sc = new SparkContext(conf)

  }

}
