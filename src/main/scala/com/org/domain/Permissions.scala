package com.org.domain

class Permissions(val permissions: Seq[Permission] = Seq()) {
  def contains(permission: Permission): Boolean = permissions.contains(permission)
}

object Permissions {
  def simpleUser(): Permissions = new Permissions(Seq())
}
