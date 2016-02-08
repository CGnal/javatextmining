package com.cgnal

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

/**
  *
  */
package object corenlp extends SprayJsonSupport with DefaultJsonProtocol {

  val HOST = "host"
  val PORT = "port"

  val PERSON = "PERSON"
  val ORGANIZATION = "ORGANIZATION"
  val LOCATION = "LOCATION"

  case class Entity(name: String, category: String)

  case class EntityText(text: String)

  case class TextAndEntities(text: String, entities: List[Entity])

  case class EntityCategoryCount(category:String,count:Int, entities:Option[List[String]]=None ,leftovers:Option[List[String]]=None)

  case class NamedEntityExtractionStats(
                                         name: String,
                                         total: Int,
                                         categories:Set[EntityCategoryCount],
                                         countGhosts:Option[Int]=None,
                                         ghosts:Option[List[String]]=None,
                                         countUnmatched:Option[Int]=None,
                                         notMatched:Option[List[String]]=None
                                       )

  case class NamedEntityExtractionResult(text:String="",scores:Set[NamedEntityExtractionStats])


  implicit val entityTextFormat = jsonFormat1(EntityText)
  implicit val entityFormat = jsonFormat2(Entity)
  implicit val textAndEntities = jsonFormat2(TextAndEntities)
  implicit val entityCategoryCountFormat = jsonFormat4(EntityCategoryCount)
  implicit val namedEntityExtractionStatsFormat = jsonFormat7(NamedEntityExtractionStats)
  implicit val namedEntityExtractionResultFormat = jsonFormat2(NamedEntityExtractionResult)


}
