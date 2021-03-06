package com.cgnal.corenlp

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import spray.json._

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext

/**
  *
  */
class WebServer(implicit val system: ActorSystem, implicit val execContext: ExecutionContext, implicit val materializer: ActorMaterializer) extends EntityExtractor with AkkaEnvironment {


  private val logger = Logger(LoggerFactory.getLogger(this.getClass))
  val configuration: Config = ConfigFactory.load()
  val alchemyEntityExtractor = new AlchemyEntityExtractor()
  val rosetteEntityExtractor = new RosetteEntityExtractor()
  val openCalaisEntityExtractor = new OpenCalaisEntityExtractor()
  val cogitoEntityExtractor = new CogitoExtractor()

  val routes = {
    pathPrefix("rest") {
      pathPrefix("entity") {
        post {
          entity(as[EntityText]) { text =>
            logger.info(s"extracting text:\n $text")
            val extractedEntities: String = extractEntitiesFromText(text.text).toJson.prettyPrint
            logger.info(s"extracted entities:\n $extractedEntities")
            complete(extractedEntities)
          }
        }
      } ~
        pathPrefix("textandentities") {
          post {
            entity(as[TextAndEntities]) {
              textAndEntities: TextAndEntities => {

                val entities: List[Entity] = textAndEntities.entities
                val entitiesByCategory: Map[String, List[Entity]] = Map("PERSON" -> Nil, "ORGANIZATION" -> Nil, "LOCATION" -> Nil) ++ entities.groupBy(_.category)

                val expectedCounts: Set[EntityCategoryCount] = entitiesByCategory.map(x => {
                  val map: List[String] = x._2.map(_.name)
                  EntityCategoryCount(x._1, x._2.length, Some(map))
                }).toSet
                val totalCount: Int = expectedCounts.foldLeft(0)(_ + _.count)
                val expectedStatistics = NamedEntityExtractionStats("SAURON", totalCount, expectedCounts)


                val text = textAndEntities.text
                /**
                  * core NLP
                  */

                val coreNLPText: Seq[Entity] = extractEntitiesFromText(text)
                val coreNLP: NamedEntityExtractionStats = computeStats("CORENLP", entitiesByCategory, coreNLPText)

                /**
                  * alchemy
                  */
                val alchemyEntities: Seq[Entity] = alchemyEntityExtractor.extractEntitiesFromText(text)
                val alchemyStats: NamedEntityExtractionStats = computeStats("ALCHEMY", entitiesByCategory, alchemyEntities)

                /**
                  * rosette
                  */
                val rosetteEntities: Seq[Entity] = rosetteEntityExtractor.extractEntitiesFromText(text)
                val rosetteStats: NamedEntityExtractionStats = computeStats("ROSETTE", entitiesByCategory, rosetteEntities)

                /**
                  * open calais
                  */
                val openCalaisEntities: Seq[Entity] = openCalaisEntityExtractor.extractEntitiesFromText(text)
                val openCalaisStats: NamedEntityExtractionStats = computeStats("OPENCALAIS", entitiesByCategory, openCalaisEntities)

                /**
                  * cogito
                  */
                val cogitoEntities: Seq[Entity] = cogitoEntityExtractor.extractEntitiesFromText(text)
                val cogitoStats: NamedEntityExtractionStats = computeStats("COGITO", entitiesByCategory, cogitoEntities)

                val result = NamedEntityExtractionResult(text = text, scores = Set(expectedStatistics, coreNLP, alchemyStats, rosetteStats, openCalaisStats, cogitoStats))

                complete(result.toJson.prettyPrint)
              }
            }
          }
        }
    }
  }

  /**
    * Compute the statistics for the named entity extractor
    *
    * @param serviceName
    * @param expectedEntitiesByCategory
    * @param extractedEntities
    * @return
    */
  def computeStats(serviceName: String, expectedEntitiesByCategory: Map[String, List[Entity]], extractedEntities: Seq[Entity]): NamedEntityExtractionStats = {

    val actualEntitiesByCategory: Map[String, Seq[Entity]] = extractedEntities.groupBy(_.category)

    val expectedCategories: Set[String] = expectedEntitiesByCategory.keySet
    val entitiesCategory: Iterator[String] = expectedCategories.iterator

    val accumulator = ListBuffer[EntityCategoryCount]()

    //entities not matched
    val entitiesNotMatchedList = ListBuffer[Entity]()
    val entitiesLeftOversList = ListBuffer[Entity]()

    while (entitiesCategory.hasNext) {

      val entityCategory: String = entitiesCategory.next()
      val expectedEntityForCategory = expectedEntitiesByCategory.getOrElse(entityCategory, List())

      val actualEntityForCategory = actualEntitiesByCategory.getOrElse(entityCategory, List())

      // those not found
      val entitiesNotMatched: Seq[Entity] = expectedEntityForCategory.diff(actualEntityForCategory)
      entitiesNotMatchedList ++= entitiesNotMatched

      // those matched not expected
      val entitiesLeftOvers: Seq[Entity] = actualEntityForCategory.diff(expectedEntityForCategory)
      entitiesLeftOversList ++= entitiesLeftOvers

      val intersection: Seq[Entity] = expectedEntityForCategory.intersect(actualEntityForCategory)

      accumulator += EntityCategoryCount(entityCategory, intersection.length, Some(intersection.map(_.name).toList))

    }

    val panieppesci = searchGhostsAndUnmatched(entitiesNotMatchedList, entitiesLeftOversList)

    val theGhosts: Seq[Entity] = panieppesci._1
    val theUnmatched: Seq[Entity] = panieppesci._2

    val length = theGhosts.length

    val totalCoreNLP: Int = accumulator.foldLeft(0)(_ + _.count)

    val coreNLP = NamedEntityExtractionStats(
      serviceName,
      totalCoreNLP,
      categories = accumulator.toSet,
      countGhosts = Some(length),
      ghosts = Some(theGhosts.map(_.name).toList),
      countUnmatched = Some(theUnmatched.length),
      notMatched = Some(theUnmatched.map(_.name).toList)
    )
    coreNLP
  }

  /**
    * Compute the statistics for the named entity extractor
    *
    * @param serviceName
    * @param expectedEntitiesByCategory
    * @param extractedEntities
    * @return
    */
  def computeAlchemyStats(serviceName: String, expectedEntitiesByCategory: Map[String, List[Entity]], extractedEntities: Seq[Entity]): NamedEntityExtractionStats = {

    val actualEntitiesByCategory: Map[String, Seq[Entity]] = extractedEntities.groupBy(_.category)

    val expectedCategories: Set[String] = expectedEntitiesByCategory.keySet
    val entitiesCategory: Iterator[String] = expectedCategories.iterator

    val accumulator = ListBuffer[EntityCategoryCount]()

    //entities not matched
    val entitiesNotMatchedList = ListBuffer[Entity]()
    val entitiesLeftOversList = ListBuffer[Entity]()

    while (entitiesCategory.hasNext) {

      val entityCategory: String = entitiesCategory.next()
      if (entityCategory.equals("PERSON")) {

        val expectedEntityForCategory: List[Entity] = expectedEntitiesByCategory.getOrElse(entityCategory, List())
        val actualEntityForCategory: Seq[Entity] = actualEntitiesByCategory.getOrElse(entityCategory, List())

        val intersection = ListBuffer[Entity]()
        val notMatched = ListBuffer[Entity]()


        expectedEntityForCategory.map(x=>{
          val filter = actualEntityForCategory.filter(z=>{ z.category.equals(x.category) &&( z.name.equals(x.name)|| z.name.contains(x.name) || x.name.contains(z.name))})

          if(filter.length>0){
            intersection += filter(0)
          }
          else {
            notMatched += x
          }
        })

        // those not found
        val entitiesNotMatched: Seq[Entity] = expectedEntityForCategory.diff(actualEntityForCategory)
        entitiesNotMatchedList ++= notMatched

        // those matched not expected
        val entitiesLeftOvers: Seq[Entity] = actualEntityForCategory.diff(expectedEntityForCategory)
        entitiesLeftOversList ++= expectedEntityForCategory.diff(intersection)

        accumulator += EntityCategoryCount(entityCategory, intersection.length, Some(intersection.map(_.name).toList))

      }
      else {
        val expectedEntityForCategory = expectedEntitiesByCategory.getOrElse(entityCategory, List())

        val actualEntityForCategory = actualEntitiesByCategory.getOrElse(entityCategory, List())

        // those not found
        val entitiesNotMatched: Seq[Entity] = expectedEntityForCategory.diff(actualEntityForCategory)
        entitiesNotMatchedList ++= entitiesNotMatched

        // those matched not expected
        val entitiesLeftOvers: Seq[Entity] = actualEntityForCategory.diff(expectedEntityForCategory)
        entitiesLeftOversList ++= entitiesLeftOvers

        val intersection: Seq[Entity] = expectedEntityForCategory.intersect(actualEntityForCategory)

        accumulator += EntityCategoryCount(entityCategory, intersection.length, Some(intersection.map(_.name).toList))
      }

    }

    val panieppesci = searchGhostsAndUnmatched(entitiesNotMatchedList, entitiesLeftOversList)

    val theGhosts: Seq[Entity] = panieppesci._1
    val theUnmatched: Seq[Entity] = panieppesci._2

    val length = theGhosts.length

    val totalCoreNLP: Int = accumulator.foldLeft(0)(_ + _.count)

    val coreNLP = NamedEntityExtractionStats(
      serviceName,
      totalCoreNLP,
      categories = accumulator.toSet,
      countGhosts = Some(length),
      ghosts = Some(theGhosts.map(_.name).toList),
      countUnmatched = Some(theUnmatched.length),
      notMatched = Some(theUnmatched.map(_.name).toList)
    )
    coreNLP
  }


  val port: Int = configuration.getInt(PORT)
  val host: String = configuration.getString(HOST)


  def searchGhostsAndUnmatched(from: Seq[Entity], to: Seq[Entity]): (Seq[Entity], Seq[Entity]) = {
    val theGhosts = (for {
      x <- from
      filtered = to.filter(_.name.equals(x.name))
      rrrr = if (filtered.isEmpty) filtered else List(filtered.head)
    } yield rrrr) flatten

    val theUnmatched = from.filter(x => !theGhosts.exists(_.name.equals(x.name)))

    (theGhosts, theUnmatched)
  }


  def start(): Unit = {
    logger.info(s"core NLP entity extractor has started on $host and listens on $port ")
    Http().bindAndHandle(routes, host, port)
  }


}



