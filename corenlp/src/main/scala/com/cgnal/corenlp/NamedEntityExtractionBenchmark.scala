package com.cgnal.corenlp

import java.io.File
import java.nio.file.{Files, Path, Paths}

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.typesafe.config.{Config, ConfigFactory}
import spray.json._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.concurrent.duration._

/**
  *
  */
object NamedEntityExtractionBenchmark extends HttpClient with AkkaEnvironment {

  def readWholeFileToString(resource: String): String = {
    new String(Files.readAllBytes(Paths.get(resource)))
  }

  implicit val system = ActorSystem()
  implicit val materializer =
    ActorMaterializer(ActorMaterializerSettings(system)
      .withInputBuffer(initialSize = 16384, maxSize = 16384 * 8))
  implicit val execContext = system.dispatcher

  val webserver = new WebServer()

  def main(args: Array[String]) {

    webserver.start()

    val load: Config = ConfigFactory.load()

    val path: Path = Paths.get(load.getString("testfiles"))
    val dir: File = path.toAbsolutePath().toFile()

    val listOfEntities: List[NamedEntityExtractionResult] = dir.listFiles().toList.sortBy(_.getName).map(file => {

      val path1: String = file.getAbsolutePath()
      val wholeFile: String = readWholeFileToString(path1)

      val pieces: List[String] = wholeFile.split("\n").toList

      val text = pieces.head
      val recognizedEntities = pieces.tail.map(s => {
        val entities = s.split("\\s+").toList
        val entityType = entities.head.trim()
        val name = entities.tail.mkString(" ").trim()
        Entity(name, entityType)
      })

      val request: HttpRequest = HttpRequest(uri = "/rest/textandentities",
        method = HttpMethods.POST,
        entity = HttpEntity(ContentTypes.`application/json`,
          TextAndEntities(text, recognizedEntities).toJson.prettyPrint))

      val call = executeCall(request)

      val result: HttpResponse = Await.result(call, 60 seconds)

      val contentType: String = Await.result(result.entity.toStrict(60 seconds).map(_.data.decodeString("UTF-8")), 60 seconds)
      contentType.parseJson.convertTo[NamedEntityExtractionResult]

    })


    val totalPercentageList: mutable.HashMap[String, ListBuffer[Double]] = scala.collection.mutable.HashMap[String, ListBuffer[Double]]()
    val personPercentageList: mutable.HashMap[String, ListBuffer[Double]] = scala.collection.mutable.HashMap[String, ListBuffer[Double]]()
    val organizationPercentageList: mutable.HashMap[String, ListBuffer[Double]] = scala.collection.mutable.HashMap[String, ListBuffer[Double]]()
    val locationPercentageList: mutable.HashMap[String, ListBuffer[Double]] = scala.collection.mutable.HashMap[String, ListBuffer[Double]]()


    totalPercentageList += "CORENLP" -> ListBuffer[Double]()
    personPercentageList += "CORENLP" -> ListBuffer[Double]()
    organizationPercentageList += "CORENLP" -> ListBuffer[Double]()
    locationPercentageList += "CORENLP" -> ListBuffer[Double]()

    totalPercentageList += "ROSETTE" -> ListBuffer[Double]()
    personPercentageList += "ROSETTE" -> ListBuffer[Double]()
    organizationPercentageList += "ROSETTE" -> ListBuffer[Double]()
    locationPercentageList += "ROSETTE" -> ListBuffer[Double]()

    totalPercentageList += "ALCHEMY" -> ListBuffer[Double]()
    personPercentageList += "ALCHEMY" -> ListBuffer[Double]()
    organizationPercentageList += "ALCHEMY" -> ListBuffer[Double]()
    locationPercentageList += "ALCHEMY" -> ListBuffer[Double]()

    totalPercentageList += "COGITO" -> ListBuffer[Double]()
    personPercentageList += "COGITO" -> ListBuffer[Double]()
    organizationPercentageList += "COGITO" -> ListBuffer[Double]()
    locationPercentageList += "COGITO" -> ListBuffer[Double]()

    totalPercentageList += "OPENCALAIS" -> ListBuffer[Double]()
    personPercentageList += "OPENCALAIS" -> ListBuffer[Double]()
    organizationPercentageList += "OPENCALAIS" -> ListBuffer[Double]()
    locationPercentageList += "OPENCALAIS" -> ListBuffer[Double]()

    listOfEntities.foreach(x => {

      val sauron: NamedEntityExtractionStats = x.scores.filter(_.name.equals("SAURON")).toList(0)
      val theOthers = x.scores.filter(!_.name.equals("SAURON"))

      val persons = sauron.categories.filter(_.category.equals("PERSON")).toList
      val organizations = sauron.categories.filter(_.category.equals("ORGANIZATION")).toList
      val locations = sauron.categories.filter(_.category.equals("LOCATION")).toList

      val howManyPersons = if (persons.size > 0) persons(0).count else 0
      val howManyOrganizations = if (organizations.size > 0) organizations(0).count else 0
      val howManyLocations = if (locations.size > 0) locations(0).count else 0

      val mapp = theOthers.map(x => {

        val percentage: Double = (x.total * 100) / sauron.total
        totalPercentageList.get(x.name).get += percentage

        val xpersons = x.categories.filter(_.category.equals("PERSON")).toList
        val xorganizations = x.categories.filter(_.category.equals("ORGANIZATION")).toList
        val xlocations = x.categories.filter(_.category.equals("LOCATION")).toList

        val xhowManyPersons = if (xpersons.size > 0) xpersons(0).count else 0
        val xhowManyOrganizations = if (xorganizations.size > 0) xorganizations(0).count else 0
        val xhowManyLocations = if (xlocations.size > 0) xlocations(0).count else 0

        val personPercentage: Double = if (howManyPersons.equals(0)) 0 else (xhowManyPersons * 100) / howManyPersons
        val locationPercentage: Double = if (howManyLocations.equals(0)) 0 else (xhowManyLocations * 100) / howManyLocations
        val organizationPercentage: Double = if (howManyOrganizations.equals(0)) 0 else (xhowManyOrganizations * 100) / howManyOrganizations

        if (howManyPersons > 0) {
          personPercentageList.get(x.name).get += personPercentage
        }

        if (howManyOrganizations > 0) {
          locationPercentageList.get(x.name).get += locationPercentage
        }

        if (howManyLocations > 0) {
          organizationPercentageList.get(x.name).get += organizationPercentage
        }
      })

      println(mapp.mkString("\n"))
      println(x.toJson.prettyPrint)

    })


    println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
    println("calculating accuracy")


    List("CORENLP", "ROSETTE", "OPENCALAIS", "ALCHEMY", "COGITO").foreach(i => {

      val totals = mean(totalPercentageList.get(i).get)
      val pers = mean(personPercentageList.get(i).get)
      val org = mean(organizationPercentageList.get(i).get)
      val log = mean(locationPercentageList.get(i).get)

      println(s"$i:\tTOTAL:$totals%\tPERSONS:$pers%\tORGANIZATION:$org%\tLOCATION:$log%")

    })

    system.shutdown()
  }


  def mean(li: Seq[Double]): Double = li.foldLeft(0.0)(_ + _) / li.length


  override val host: String = "localhost"
  override val port: Int = 8080
}

