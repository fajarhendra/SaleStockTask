package com.org.service

import java.util.UUID
import javax.inject.{Inject, Singleton}

import com.org.domain.Users
import com.org.util.QueryUtil
//import com.softegra.util.QueryUtil
import com.twitter.util.Future


@Singleton
class UserService {

  def users(): Future[Seq[Users]] = QueryUtil.list[Users]()

  def findByUsername(username: String): Future[Option[Users]] =
    QueryUtil.listBy[Users](Seq(("name", "=", username))).map(x => x.headOption)


//  todo perlu di test UUID
  def findByID(id: UUID): Future[Option[Users]] =
    QueryUtil.listBy[Users](Seq(("name", "=", id))).map(x => x.headOption)


//  def users(): Future[Seq[Users]] = ???
//  def findByUsername(username: String): Future[Option[Users]] = ???
//  def findByID(id: UUID): Future[Option[Users]] = ???

}
