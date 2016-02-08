package com.cgnal.corenlp

import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.`Content-Type`
import com.typesafe.config.{ConfigFactory, Config}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite}
import org.slf4j.LoggerFactory


import akka.http.scaladsl.{model, Http}
import akka.stream.{Materializer, ActorMaterializer}
import akka.stream.scaladsl._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

import akka.http.scaladsl.model._

/**
  *
  */
class NamedEntityExtractionTest extends FunSuite with BeforeAndAfterEach with BeforeAndAfterAll with HttpClient {

  import akka.http.scaladsl.unmarshalling.Unmarshal

  private val logger = LoggerFactory.getLogger(this.getClass)

  val webserver = WebServer
  webserver.start()

  val host = "localhost"
  val port = 8080

  val responseFuture: Future[HttpResponse] =
    Source.single(HttpRequest(uri = "/"))
      .via(connectionFlow)
      .runWith(Sink.head)

  def callTextEntities(textAndEntities: TextAndEntities) =
    executeCall(HttpRequest(
      uri = "/rest/textandentities",
      method = HttpMethods.POST,
      entity = HttpEntity(ContentTypes.`application/json`, textAndEntities.toJson.toString())
    )
    )

  override protected def beforeAll(): Unit = {

  }

  override protected def afterAll(): Unit = {
  }


  override protected def beforeEach(): Unit = {
  }

  override protected def afterEach(): Unit = {

  }

  val canada: Entity = Entity("Canada", "LOCATION")
  val usa: Entity = Entity("USA", "LOCATION")
  val obama: Entity = Entity("Obama", "PERSON")
  val cgnal: Entity = Entity("CGnal", "ORGANIZATION")
  val eligotech: Entity = Entity("Eligotech", "ORGANIZATION")

  test("grouping the entities by type") {

    val entities = List[Entity](
      canada,
      usa,
      obama,
      cgnal
    )

    val by: Map[String, List[Entity]] = entities.groupBy(_.category)

    by.get("LOCATION") match {
      case Some(list) => {
        val set = list.toSet
        assert(set.contains(canada))
        assert(set.contains(usa))
      }
      case None => assert(false, "cannot group the locations")
    }

    by.get("PERSON") match {
      case Some(list) => {
        val set = list.toSet
        assert(set.contains(obama))
      }
      case None => assert(false, "cannot group the persons")
    }
  }


  test("difference of list") {
    assert(List(canada, usa, obama).diff(List(obama, cgnal, eligotech)).equals(List(canada, usa)))
  }

  test("test entity extraction") {
    assert(List(canada, usa, obama).diff(List(obama, cgnal, eligotech)).equals(List(canada, usa)))
  }

  test("\"Bob Marley lives in California\" should return a person and a location") {

    val requestBody = TextAndEntities(
      text = "Bob Marley lives in California.",
      List(
        Entity("Bob Marley", "PERSON"),
        Entity("California", "LOCATION")
      ))

    val response: Future[HttpResponse] = callTextEntities(requestBody)
    val result: HttpResponse = Await.result(response, 20 seconds)

    result match {
      case HttpResponse(StatusCodes.OK, _, re, _) => {

        val eventualResult1: Future[NamedEntityExtractionResult] = Unmarshal(re.withContentType(ContentTypes.`application/json`)).to[NamedEntityExtractionResult]
        val result1: NamedEntityExtractionResult = Await.result(eventualResult1, 20 seconds)
        val expected: Set[NamedEntityExtractionStats] = Set(
          NamedEntityExtractionStats("SAURON", 2, Set(EntityCategoryCount("PERSON", 1),
            EntityCategoryCount("LOCATION", 1))),
          NamedEntityExtractionStats("CORENLP", 2, Set(EntityCategoryCount("PERSON", 1),
            EntityCategoryCount("LOCATION", 1))),
          NamedEntityExtractionStats("ALCHEMY", 2, Set(EntityCategoryCount("PERSON", 1),
            EntityCategoryCount("LOCATION", 1))),
          NamedEntityExtractionStats("ROSETTE", 2, Set(EntityCategoryCount("PERSON", 1),
            EntityCategoryCount("LOCATION", 1))))
        assertResult(expected)(result1.scores)
      }
      case _ => assert(false)
    }
  }

  test("test an entity for each category") {

    val requestBody = TextAndEntities(
      text = "Bob Marley lives in California and works for Microsoft.",
      List(
        Entity("Bob Marley", "PERSON"),
        Entity("California", "LOCATION"),
        Entity("Microsoft", "ORGANIZATION")
      ))

    val response: Future[HttpResponse] = callTextEntities(requestBody)
    val result: HttpResponse = Await.result(response, 20 seconds)

    result match {
      case HttpResponse(StatusCodes.OK, _, re, _) => {

        val eventualResult1: Future[NamedEntityExtractionResult] = Unmarshal(re.withContentType(ContentTypes.`application/json`)).to[NamedEntityExtractionResult]
        val result1: NamedEntityExtractionResult = Await.result(eventualResult1, 20 seconds)
        val expected: Set[NamedEntityExtractionStats] = Set(
          NamedEntityExtractionStats("SAURON", 3, Set(EntityCategoryCount("PERSON", 1), EntityCategoryCount("LOCATION", 1), EntityCategoryCount("ORGANIZATION", 1))),
          NamedEntityExtractionStats("ALCHEMY", 3, Set(EntityCategoryCount("PERSON", 1), EntityCategoryCount("LOCATION", 1), EntityCategoryCount("ORGANIZATION", 1))),
          NamedEntityExtractionStats("ROSETTE", 3, Set(EntityCategoryCount("PERSON", 1), EntityCategoryCount("LOCATION", 1), EntityCategoryCount("ORGANIZATION", 1))),
          NamedEntityExtractionStats("CORENLP", 3, Set(EntityCategoryCount("PERSON", 1),
            EntityCategoryCount("LOCATION", 1), EntityCategoryCount("ORGANIZATION", 1)))
        )
        assertResult(expected)(result1.scores)
      }
      case _ => assert(false)
    }
  }

  test("testing duplicates") {

    val requestBody = TextAndEntities(
      text = "Bob Marley lives in California and works for Microsoft. Bob Marley plays bongs on the beach.",
      List(
        Entity("Bob Marley", "PERSON"),
        Entity("Bob Marley", "PERSON"),
        Entity("California", "LOCATION"),
        Entity("Microsoft", "ORGANIZATION")
      ))

    val response: Future[HttpResponse] = callTextEntities(requestBody)
    val result: HttpResponse = Await.result(response, 20 seconds)

    result match {
      case HttpResponse(StatusCodes.OK, _, re, _) => {

        val eventualResult1: Future[NamedEntityExtractionResult] = Unmarshal(re.withContentType(ContentTypes.`application/json`)).to[NamedEntityExtractionResult]
        val result1: NamedEntityExtractionResult = Await.result(eventualResult1, 20 seconds)
        val expected: Set[NamedEntityExtractionStats] = Set(
          NamedEntityExtractionStats("SAURON", 4, Set(EntityCategoryCount("PERSON", 2), EntityCategoryCount("LOCATION", 1), EntityCategoryCount("ORGANIZATION", 1))),
          NamedEntityExtractionStats("ALCHEMY", 4, Set(EntityCategoryCount("PERSON", 2), EntityCategoryCount("LOCATION", 1), EntityCategoryCount("ORGANIZATION", 1))),
          NamedEntityExtractionStats("ROSETTE", 4, Set(EntityCategoryCount("PERSON", 2), EntityCategoryCount("LOCATION", 1), EntityCategoryCount("ORGANIZATION", 1))),
          NamedEntityExtractionStats("CORENLP", 4, Set(EntityCategoryCount("PERSON", 2), EntityCategoryCount("LOCATION", 1), EntityCategoryCount("ORGANIZATION", 1)))
        )
        assertResult(expected)(result1.scores)
      }
      case _ => assert(false)
    }
  }


}

