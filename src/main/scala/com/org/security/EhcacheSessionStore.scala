package com.org.security

import java.util.UUID

import net.sf.ehcache.{Cache, CacheManager}

import scalacache.ScalaCache
import scalacache.ehcache.EhcacheCache

trait SessionStore {
  def put(token: SessionID, userId: UUID)

  def resolveUser(token: SessionID): Option[UUID]

  def remove(token: SessionID)
}

class EhcacheSessionStore() extends SessionStore {

  val cacheManager: CacheManager =  CacheManager.getInstance()
  val underlying: Cache = cacheManager.getCache("myCache")

  implicit val scalaCache = ScalaCache(EhcacheCache(underlying))


  def put(token: SessionID, userId: UUID) = scalacache.put(token.value)(userId.toString)

  def resolveUser(token: SessionID): Option[UUID] = scalacache.sync.get (token.value).map(userID => UUID.fromString(userID))

  def remove(token: SessionID) =  scalacache.remove(token.value)
}
