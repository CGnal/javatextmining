package com.cgnal.corenlp

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.ContentType.WithCharset
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.typesafe.config.{Config, ConfigFactory}
import spray.json._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

/**
  *
  */
class CogitoExtractor(implicit val system: ActorSystem, implicit val execContext: ExecutionContext, implicit val materializer: ActorMaterializer) extends NamedEntityExtractor with HttpClient with SprayJsonSupport with DefaultJsonProtocol {


  private val config: Config = ConfigFactory.load()

  private val alchemiApi: String = config.getString("alchemyapi")

  val host = "services.cogitoapi.com"
  val port = 443

  val url = "/1.1/kernel/categorization"

  val people = "/1.1/kernel/ext/people"
  val places = "/1.1/kernel/ext/places"
  val organizations = "/1.1/kernel/ext/organizations"

  private val cogitoapi: String = config.getString("cogitoapi")

  val entityTypesMap = Map(
  )


  def createHttpRequest(uri: String, text: String) = HttpRequest(
    uri = uri,
    method = HttpMethods.POST,
    headers = List(RawHeader("Apikey", cogitoapi)),
    entity = HttpEntity(WithCharset(MediaTypes.`application/x-www-form-urlencoded`, HttpCharsets.`UTF-8`), s"text=$text".getBytes)
  )


  override def extractEntitiesFromText(text: String): Seq[Entity] = {

    //people
    val peopleCall = executeHttpsCall(createHttpRequest(people, text))
    val peopleResult: HttpResponse = Await.result(peopleCall, 60 seconds)
    val peopleString: String = Await.result(peopleResult.entity.toStrict(30 seconds).map(_.data.decodeString("UTF-8")), 30 seconds)
    val response: CogitoResponse = peopleString.parseJson.convertTo[CogitoResponse]
    val a = response.analysisResult.extraction.entities.flatMap(x => x.matches.map(z=>Entity(z.value, "PERSON")))

    //places
    val placesCall = executeHttpsCall(createHttpRequest(places, text))
    val placesResult: HttpResponse = Await.result(placesCall, 60 seconds)
    val placesString: String = Await.result(placesResult.entity.toStrict(30 seconds).map(_.data.decodeString("UTF-8")), 30 seconds)
    val response2: CogitoResponse = placesString.parseJson.convertTo[CogitoResponse]
    val b = response2.analysisResult.extraction.entities.flatMap(x => x.matches.map(z=>Entity(z.value, "LOCATION")))

    //organizations
    val organizationsCall = executeHttpsCall(createHttpRequest(organizations, text))
    val organizationsResult: HttpResponse = Await.result(organizationsCall, 60 seconds)
    val organizationsString: String = Await.result(organizationsResult.entity.toStrict(30 seconds).map(_.data.decodeString("UTF-8")), 30 seconds)
    val response3: CogitoResponse = organizationsString.parseJson.convertTo[CogitoResponse]
    val c = response3.analysisResult.extraction.entities.flatMap(x => x.matches.map(z=>Entity(z.value, "ORGANIZATION")))

    a ++ b ++ c
  }

  case class CogitoEntityMatch(start: Int, end: Int, value: String)
  implicit val cogitoEntityMatchFormat = jsonFormat3(CogitoEntityMatch)

  case class CogitoAnalysisEntity(text: String, matches: List[CogitoEntityMatch])
  implicit val cogitoAnalysisEntityFormat = jsonFormat2(CogitoAnalysisEntity)

  case class CogitoAnalysisExtraction(entities: List[CogitoAnalysisEntity])
  implicit val cogitoAnalysisEntityExtractionFormat = jsonFormat1(CogitoAnalysisExtraction)

  case class CogitoAnalysisResult(extraction: CogitoAnalysisExtraction)
  implicit val cogitoAnalysisResultFormat = jsonFormat1(CogitoAnalysisResult)

  case class CogitoResponse(cleanText: Option[String], analysisResult: CogitoAnalysisResult)
  implicit val cogitoAnalysisResponseFormat = jsonFormat2(CogitoResponse)
  //  implicit object CogitoAnalysisEntity extends RootJsonFormat[CogitoAnalysisEntity] {
  //
  //    def write(c: CogitoAnalysisEntity) =
  //    //FIXME no need to read
  //      JsObject(
  //
  //      )
  //
  //    def read(value: JsValue) = {
  //      value.asJsObject.getFields("text") match {
  //        case Seq(JsString(text)) =>
  //          CogitoAnalysisEntity(text)
  //        case _ => throw new DeserializationException("Color expected")
  //      }
  //    }
  //  }
}

object CogitoExtractor extends App {

  implicit val system = ActorSystem()
  implicit val materializer =
    ActorMaterializer(ActorMaterializerSettings(system)
      .withInputBuffer(initialSize = 16384, maxSize = 16384 * 8))
  implicit val execContext = system.dispatcher

  private val extractor = new CogitoExtractor()
  private val text: Seq[Entity] = extractor.extractEntitiesFromText("Nine major banks have joined with fintech startup R3CEV LLC (R3) to develop a framework for using Blockchain technology in financial markets. The announcement of the partnership to design and deliver advanced distributed/shared ledger technologies to global financial markets includes some of the biggest names in global banking, including Barclays, BBVA, Commonwealth Bank of Australia (CBA), Credit Suisse, J.P. Morgan, State Street, Royal Bank of Scotland and UBS. Utilizing the Blockchain, the digital ledger that underpins Bitcoin, the group plans to collaborate on research, experimentation, design, and engineering with an aim of delivering advanced state-of-the-art enterprise-scale shared ledger solutions that also meet banking requirements for security, reliability, performance, scalability, and audit. Led by R3, the group will establish collaborative joint working groups to develop these efforts; many, if not all of its members have previously worked on ways of using the Blockchain in everyday business, with the likes of the CBA working on Blockchain financial settlements, and Barclays even going as far as accepting Bitcoin payments. Kevin Hanley, Director of Design at Royal Bank of Scotland explained why the group is coming together, saying in a statement  Right now you’re seeing significant money and time being spent on exploration of these technologies in a fractured way that lacks the strategic, coordinated vision so critical to timely success. The R3 model is changing the game. The collaborative model we’ve established with R3 and the other banks is a very effective way to deliver robust shared ledger solutions to the financial services sector,  he said. Saving money. Cutting out the middleman in financial transactions is all about saving money, and that’s what implementation of the Blockchain would mean for these big banks, let alone the fact that Blockchain transactions are quicker and more secure as well. These new technologies could transform how financial transactions are recorded, reconciled and reported…  Senior Vice President and head of Emerging Technologies at State Street Hu Liang added,  … all with additional security, lower error rates and significant cost reductions. The step to work together on the implementation of the Blockchain in financial markets is logical as banks and other financial institutions would need to agree to standards eventually, so it makes sense that some of the big names would be involved collaboratively in developing those standards. There were no time frames given by the group in terms of targets, however in theory working together on the Blockchain could see the platform takeover financial markets even quicker than many had already expected.")

  system.shutdown()

  println(text)


}



