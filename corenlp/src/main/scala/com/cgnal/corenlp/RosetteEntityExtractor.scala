package com.cgnal.corenlp

import java.net.URLEncoder

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.HttpHeader.ParsingResult
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.typesafe.config.{Config, ConfigFactory}
import spray.json._

import scala.collection.immutable.IndexedSeq
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  *
  */
object RosetteEntityExtractor extends NamedEntityExtractor with HttpClient with SprayJsonSupport with DefaultJsonProtocol {

  private val config: Config = ConfigFactory.load()

  private val rosetteApi: String = config.getString("rosetteapi")

  val host = "api.rosette.com"
  val port = 443

  val entityTypesMap = Map(
    "PERSON" -> "PERSON",
    "LOCATION" -> "LOCATION",
    "ORGANIZATION" -> "ORGANIZATION"
  )

  val url = "/rest/v1/entities"


  override def extractEntitiesFromText(text: String): Seq[Entity] = {

    val request: HttpRequest = HttpRequest(uri = url,
      method = HttpMethods.POST,
      headers = List(RawHeader("user_key",rosetteApi)),
      entity = HttpEntity(ContentTypes.`application/json`, RosetteContent(content = text).toJson.prettyPrint))

    val call: Future[HttpResponse] = executeHttpsCall(request)

    val result: HttpResponse = Await.result(call, 30 seconds)

    val eventualResult1: Future[RosetteResponse] = Unmarshal(result.entity.withContentType(ContentTypes.`application/json`)).to[RosetteResponse]

    val result1: RosetteResponse = Await.result(eventualResult1, 30 seconds)

    val convertedResult = result1.entities.flatMap { e =>

      val howMany = e.count

      val entities: IndexedSeq[Entity] = (1 to howMany).map(r => Entity(e.mention, entityTypesMap.getOrElse(e.entityType, "OTHER"))).filter(_.category != "OTHER")

      entities
    }
    convertedResult
  }


  def main(args: Array[String]) {
    val text: Seq[Entity] = extractEntitiesFromText("Bob Marley lives in California.")
    println(text.toJson.prettyPrint)

  }

  case class RosetteContent(content:String)
  case class RosetteResponse(requestId:String,entities:List[RosetteEntity])

  case class RosetteEntity(indoChainId:Int,entityType:String,mention:String,normalized:String,count:Int,confidence:Double)

  implicit object RosetteEntityFormat extends RootJsonFormat[RosetteEntity] {

    def write(c: RosetteEntity) =
      JsObject(
        "indocChainId" -> JsNumber(c.indoChainId),
        "type" -> JsString(c.entityType),
        "mention" -> JsString(c.mention),
        "normalized" -> JsString(c.normalized),
        "count" -> JsNumber(c.count),
        "confidence" -> JsNumber(c.confidence)
      )

    def read(value: JsValue) = {
      value.asJsObject.getFields("indocChainId", "type", "mention", "normalized","count","confidence") match {
        case Seq(JsNumber(indo), JsString(t),JsString(me),JsString(no),JsNumber(cou),JsNumber(conf)) =>
          new RosetteEntity(indo.toInt,t,me,no,cou.toInt,conf.toDouble)
        case _ => throw new DeserializationException("Color expected")
      }
    }
  }

  implicit val rosetteContentFormat = jsonFormat1(RosetteContent)
  implicit val rosetteResponseFormat = jsonFormat2(RosetteResponse)

  //  {
//    "requestId": "e136c90d-8aeb-4049-88b2-aa896862d5bb",
//    "entities": [
//    {
//      "indocChainId": 0,
//      "type": "LOCATION",
//      "mention": "London",
//      "normalized": "London",
//      "count": 2,
//      "confidence": 0.030316948890686035
//    },


}
