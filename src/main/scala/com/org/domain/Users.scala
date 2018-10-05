package com.org.domain

import java.util.UUID
import java.util.UUID._


//todo permission memakai array, https://www.postgresql.org/docs/9.1/static/arrays.html
/**
  *
  * @param id
  * @param username
  * @param password
  * @param permissions pakai tipe data String karena sebenarnya collection
  * @param userGroupId com.softegra.domain.Model.UserGroup
  */
case class Users(id: UUID = randomUUID(), username: String, password: String, var permissions: String = "",
                var userGroupId: UUID) {

  /**
    * teknik general untuk property dg tipe data colletion. property-nya harus disimpan dalam format text
    *
    * @return
    */
  def listRules: Seq[UserRule.Value] = {
    (Seq[UserRule.Value]() /: permissions.split(",")) { (seq, rule) =>
      seq :+ UserRule.withName(rule)
    }
  }

  def setRules(ruleSeq: Seq[UserRule.Value]) {
    this.permissions = ruleSeq.distinct.mkString(",")
  }

  def addRule(rule: UserRule.Value): Unit = {
    val rulesAll = listRules :+ rule
    this.permissions = rulesAll.distinct.mkString(",")
  }

  def removeRule(rule: UserRule.Value): Unit = {
    val rulesAll = listRules.filter(_ != rule)
    this.permissions = rulesAll.distinct.mkString(",")

  }


  //  def hasPermissionTo(permission: Permission): Boolean = permissions.contains(permission)

}


/**
  * tipe rule bawaan template, belum kepikiran penggunaannya
  */
object UserRule extends Enumeration {

  val Admin, Business, Network = Value
}


