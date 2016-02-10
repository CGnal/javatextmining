package com.cgnal.corenlp

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite}
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  *
  */
class AlchemyExtractorTest extends FunSuite with BeforeAndAfterEach with BeforeAndAfterAll  {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(system)
    .withInputBuffer(initialSize = 16384, maxSize = 16384 * 8))
  implicit val execContext = system.dispatcher

  import akka.http.scaladsl.unmarshalling.Unmarshal

  private val logger = LoggerFactory.getLogger(this.getClass)


  override protected def beforeAll(): Unit = {

  }

  override protected def afterAll(): Unit = {
  }


  override protected def beforeEach(): Unit = {
  }

  override protected def afterEach(): Unit = {

  }

  val barackObama: Entity = Entity("Barack Obama", "PERSON")
  val obama: Entity = Entity("Obama", "PERSON")
  val cgnal: Entity = Entity("CGnal", "ORGANIZATION")
  val eligotech: Entity = Entity("Eligotech", "ORGANIZATION")

  test("single entities should not be enriched") {

    val entities = List(obama,cgnal,eligotech)

    val homonimous = AlchemyEntityExtractor.enrichEntitiesWithHomonimous(entities)

    assertResult(entities.toSet)(homonimous.toSet)

  }

  test("enriching homonimous 0") {

    val entities = List(barackObama,obama,cgnal,eligotech)

    val homonimous = AlchemyEntityExtractor.enrichEntitiesWithHomonimous(entities)

    assert( homonimous.filter(_.equals(barackObama)).length == 2)
    assert( homonimous.filter(_.equals(obama)).length == 2)

  }

  test("enriching homonimous") {

    val entities = List(barackObama,barackObama,obama,cgnal,eligotech)

    val homonimous = AlchemyEntityExtractor.enrichEntitiesWithHomonimous(entities)

    assert( homonimous.filter(_.equals(barackObama)).length == 3)
    assert( homonimous.filter(_.equals(obama)).length == 3)

  }


  test("enriching homonimous 2") {

    val entities = List(barackObama,barackObama,barackObama,obama,cgnal,eligotech)

    val homonimous = AlchemyEntityExtractor.enrichEntitiesWithHomonimous(entities)

    assert( homonimous.filter(_.equals(barackObama)).length == 4)
    assert( homonimous.filter(_.equals(obama)).length == 4)

  }


  test("enriching homonimous 3") {

    val entities = List(barackObama,barackObama,barackObama,obama,obama,cgnal,eligotech)

    val homonimous = AlchemyEntityExtractor.enrichEntitiesWithHomonimous(entities)

    assert( homonimous.filter(_.equals(barackObama)).length == 4)
    assert( homonimous.filter(_.equals(obama)).length == 4)

  }



}

