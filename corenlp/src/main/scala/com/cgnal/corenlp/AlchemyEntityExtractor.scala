package com.cgnal.corenlp

import java.net.URLEncoder

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import spray.json._

import scala.collection.immutable.IndexedSeq
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

object AlchemyEntityExtractor {


  def enrichEntitiesWithHomonimous(entities: Seq[Entity]): Seq[Entity] = {

    val akky = ListBuffer[Entity]()
    val alreadyGot = ListBuffer[Entity]()

    entities foreach { x =>
      if (!(alreadyGot contains x)) {

        val filtered = entities filter { y => ( x.name.contains(y.name) || y.name.contains(x.name)) && !y.name.equals(x.name )}

        if (!filtered.isEmpty) {
          val howManyOfThese = entities filter { z => z.name.equals(x.name) }

            akky ++= (0 to modulus(filtered.length-howManyOfThese.length)).map(u=>x)

          alreadyGot += x
        }
      }
    }
    akky ++= entities
    akky
  }

  def modulus(x:Int):Int = if(x>0)x else -x


}


/**
  *
  */
class AlchemyEntityExtractor(implicit val system: ActorSystem, implicit val execContext: ExecutionContext, implicit val materializer: ActorMaterializer) extends NamedEntityExtractor with HttpClient with SprayJsonSupport with DefaultJsonProtocol {

  private val config: Config = ConfigFactory.load()

  private val alchemiApi: String = config.getString("alchemyapi")

  val host = "access.alchemyapi.com"
  val port = 80

  val entityTypesMap = Map(
    "Person" -> "PERSON",
    "StateOrCounty" -> "LOCATION",
    "Country" -> "LOCATION",
    "GeographicFeature" -> "LOCATION",
    "City" -> "LOCATION",
    "Company" -> "ORGANIZATION",
    "Organization" -> "ORGANIZATION"
  )

  val url = "/calls/text/TextGetRankedNamedEntities?" + s"apikey=$alchemiApi" + "&outputMode=json&" + "text="

  override def extractEntitiesFromText(text: String): Seq[Entity] = {

    val request: HttpRequest = HttpRequest(uri = url + URLEncoder.encode(text, "UTF-8"))

    val call: Future[HttpResponse] = executeCall(request)

    val result: HttpResponse = Await.result(call, 60 seconds)

    val eventualResult1: Future[AlchemyResponse] = Unmarshal(result.entity.withContentType(ContentTypes.`application/json`)).to[AlchemyResponse]

    val result1: AlchemyResponse = Await.result(eventualResult1, 60 seconds)

    val convertedResult = result1.entities.flatMap { e =>

      val howMany = e.count.toInt

      val entities: IndexedSeq[Entity] = (1 to howMany).map(r => Entity(e.text, entityTypesMap.getOrElse(e.entityType, "OTHER"))).filter(_.category != "OTHER")

      AlchemyEntityExtractor.enrichEntitiesWithHomonimous(entities)
    }
    convertedResult
  }


  def main(args: Array[String]) {
    extractEntitiesFromText("Bob Marley lives in California.")
  }


  case class AlchemyEntityDisambiguated(
                                         subType: Option[List[String]],
                                         name: String,
                                         website: Option[String],
                                         dbpedia: Option[String],
                                         freebase: Option[String],
                                         yago: Option[String],
                                         musicBrainz: Option[String])

  case class AlchemyEntity(
                            entityType: String,
                            relevance: String,
                            count: String,
                            text: String,
                            disambiguated: Option[AlchemyEntityDisambiguated] = None)

  case class AlchemyResponse(status: String, usage: String, url: String, language: String, entities: List[AlchemyEntity])


  implicit object AlchemyEntityFormat extends RootJsonFormat[AlchemyEntity] {

    def write(c: AlchemyEntity) =
      JsObject(
        "type" -> JsString(c.entityType),
        "relevance" -> JsString(c.relevance),
        "count" -> JsString(c.count),
        "text" -> JsString(c.text),
        "disambiguated" -> c.disambiguated.toJson
      )

    def read(value: JsValue) = {
      value.asJsObject.getFields("type", "relevance", "count", "text", "disambiguated") match {
        case Seq(JsString(entityType), JsString(relevance), JsString(count), JsString(text)) =>
          new AlchemyEntity(entityType = entityType, relevance = relevance, count = count, text = text)

        case Seq(JsString(entityType), JsString(relevance), JsString(count), JsString(text), disambiguated) =>
          new AlchemyEntity(entityType = entityType, relevance = relevance, count = count, text = text, disambiguated = Some(disambiguated.convertTo[AlchemyEntityDisambiguated]))
        case _ => throw new DeserializationException("Color expected")
      }
    }
  }

  implicit val alchemyResponseFormat = jsonFormat5(AlchemyResponse)
  implicit val alchemyResponseDisambiguatedFormat = jsonFormat7(AlchemyEntityDisambiguated)


  //  {
  //    "status": "OK",
  //    "usage": "By accessing AlchemyAPI or using information generated by AlchemyAPI, you are agreeing to be bound by the AlchemyAPI Terms of Use: http://www.alchemyapi.com/company/terms.html",
  //    "url": "",
  //    "language": "english",
  //    "entities": [
  //    {
  //      "type": "Person",
  //      "relevance": "0.33",
  //      "count": "1",
  //      "text": "Bob Marley",
  //      "disambiguated": {
  //        "subType": [
  //        "MusicalArtist",
  //        "Celebrity",
  //        "Guitarist",
  //        "HallOfFameInductee",
  //        "MusicalGroupMember",
  //        "FilmActor",
  //        "Lyricist"
  //        ],
  //        "name": "Bob Marley",
  //        "website": "http://www.bobmarley.com/",
  //        "dbpedia": "http://dbpedia.org/resource/Bob_Marley",
  //        "freebase": "http://rdf.freebase.com/ns/m.0bkf4",
  //        "yago": "http://yago-knowledge.org/resource/Bob_Marley",
  //        "musicBrainz": "http://zitgist.com/music/artist/ed2ac1e9-d51d-4eff-a2c2-85e81abd6360"
  //      }
  //    },
  //    {
  //      "type": "StateOrCounty",
  //      "relevance": "0.33",
  //      "count": "1",
  //      "text": "California"
  //    }
  //    ]
  //  }
}
