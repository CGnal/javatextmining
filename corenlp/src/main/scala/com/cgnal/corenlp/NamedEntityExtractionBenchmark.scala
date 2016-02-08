package com.cgnal.corenlp

import java.io.File
import java.nio.file.{Files, Path, Paths}

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializerSettings, ActorMaterializer}
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  *
  */
object NamedEntityExtractionBenchmark extends HttpClient  with AkkaEnvironment{

  def readWholeFileToString(resource: String): String = {
    new String(Files.readAllBytes(Paths.get(resource)))
  }

  implicit val system = ActorSystem()
  implicit val materializer =
    ActorMaterializer(ActorMaterializerSettings(system)
      .withInputBuffer(initialSize = 16384, maxSize = 16384*8))
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
      val eventualResult1: Future[NamedEntityExtractionResult] = Unmarshal(result.entity.withContentType(ContentTypes.`application/json`)).to[NamedEntityExtractionResult]
      Await.result(eventualResult1, 60 seconds)
    })

    listOfEntities.foreach(x=>{

      val sauron: NamedEntityExtractionStats = x.scores.filter(_.name.equals("SAURON")).toList(0)
      val theOthers = x.scores.filter(!_.name.equals("SAURON"))

      val persons = sauron.categories.filter(_.category.equals("PERSON")).toList
      val organizations = sauron.categories.filter(_.category.equals("ORGANIZATION")).toList
      val locations = sauron.categories.filter(_.category.equals("LOCATION")).toList

      val howManyPersons = if(persons.size>0) persons(0).count else 0
      val howManyOrganizations = if(organizations.size>0) organizations(0).count else 0
      val howManyLocations = if(locations.size>0) locations(0).count else 0

      val mapp = theOthers.map(x => {
        val percentage: Double = (x.total * 100) / sauron.total

        val xpersons = x.categories.filter(_.category.equals("PERSON")).toList
        val xorganizations = x.categories.filter(_.category.equals("ORGANIZATION")).toList
        val xlocations = x.categories.filter(_.category.equals("LOCATION")).toList

        val xhowManyPersons = if (xpersons.size > 0) xpersons(0).count else 0
        val xhowManyOrganizations = if (xorganizations.size > 0) xorganizations(0).count else 0
        val xhowManyLocations = if (xlocations.size > 0) xlocations(0).count else 0

        val personPercentage: Double = if (howManyPersons.equals(0)) 0 else (xhowManyPersons * 100) / howManyPersons
        val locationPercentage: Double = if (howManyLocations.equals(0)) 0 else (xhowManyLocations * 100) / howManyLocations
        val organizationPercentage: Double = if (howManyOrganizations.equals(0)) 0 else (xhowManyOrganizations * 100) / howManyOrganizations

        s"extractor ${x.name}\t total percentage: $percentage\t person %: $personPercentage\t location %: $locationPercentage \t organization % $organizationPercentage "
      })

      println(mapp.mkString("\n"))
      println(x.toJson.prettyPrint)

    })

    system.shutdown()
  }

  override val host: String = "localhost"
  override val port: Int = 8080
}

