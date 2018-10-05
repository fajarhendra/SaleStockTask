package com.org

import com.twitter.finatra.request._
import com.twitter.finatra.validation._
import com.twitter.inject.domain.WrappedValue

/**
  * Created by subhan on 1/16/17.
  */
package object controller {


  case class GetPostsRequest(@Min(1) @Max(100) @QueryParam limit: Int = 10)

  case class CreatePostRequest(@NotEmpty title: String)

  case class UpdatePostRequest(@RouteParam id: UUID, title: String)

  case class DeletePostRequest(@RouteParam id: UUID)


  case class Tweet(
                    @Size(min = 1, max = 140)message: String,
                    location: Option[Location],
                    nsfw: Boolean = false)

  case class TweetId(val id: String) extends WrappedValue[String]

  case class TweetGetRequest (
                               @RouteParam id: TweetId)

  case class TweetsRequest(
                            @Max(100) @QueryParam max: Int)

  case class TweetResponse(
                            message: String,
                            location: Option[TweetLocation],
                            nsfw: Boolean) {

    def toDomain = {
      Tweet(
        message,
        location map { _.toDomain },
        nsfw)
    }
  }


  case class TweetLocation(
                            @Range(min = -90, max = 90) lat: Double,
                            @Range(min = -180, max = 180) long: Double) {

    def toDomain = {
      Location(lat, long)
    }
  }

  case class Location(
                       @Range(min = -90, max = 90) lat: Double,
                       @Range(min = -180, max = 180) long: Double)

}
