package com.cgnal.corenlp

import java.net.URLEncoder

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.HttpEntity.Strict
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.cgnal.corenlp.RosetteEntityExtractor.RosetteContent
import com.typesafe.config.{Config, ConfigFactory}
import spray.json._

import scala.collection.immutable.IndexedSeq
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  *
  */
object OpenCalaisEntityExtractor extends NamedEntityExtractor with HttpClient with SprayJsonSupport with DefaultJsonProtocol {

  private val config: Config = ConfigFactory.load()

  private val alchemiApi: String = config.getString("opencalaisapi")

  val host = "api.thomsonreuters.com"
  val port = 443

  val entityTypesMap = Map(
    "Person" -> "PERSON",
    "Company" -> "ORGANIZATION",
    "Organization" -> "ORGANIZATION",
    "Position" -> "ORGANIZATION",
    "Country" -> "LOCATION",
    "Continent" -> "LOCATION",
    "ProvinceOrState" -> "LOCATION",
    "City" -> "LOCATION"
  )

  val url = "/permid/calais"





  def main(args: Array[String]) {

    val text = extractEntitiesFromText("Nine major banks have joined with fintech startup R3CEV LLC (R3) to develop a framework for using Blockchain technology in financial markets. The announcement of the partnership to design and deliver advanced distributed/shared ledger technologies to global financial markets includes some of the biggest names in global banking, including Barclays, BBVA, Commonwealth Bank of Australia (CBA), Credit Suisse, J.P. Morgan, State Street, Royal Bank of Scotland and UBS. Utilizing the Blockchain, the digital ledger that underpins Bitcoin, the group plans to collaborate on research, experimentation, design, and engineering with an aim of delivering advanced state-of-the-art enterprise-scale shared ledger solutions that also meet banking requirements for security, reliability, performance, scalability, and audit. Led by R3, the group will establish collaborative joint working groups to develop these efforts; many, if not all of its members have previously worked on ways of using the Blockchain in everyday business, with the likes of the CBA working on Blockchain financial settlements, and Barclays even going as far as accepting Bitcoin payments. Kevin Hanley, Director of Design at Royal Bank of Scotland explained why the group is coming together, saying in a statement  Right now you’re seeing significant money and time being spent on exploration of these technologies in a fractured way that lacks the strategic, coordinated vision so critical to timely success. The R3 model is changing the game. The collaborative model we’ve established with R3 and the other banks is a very effective way to deliver robust shared ledger solutions to the financial services sector,  he said. Saving money. Cutting out the middleman in financial transactions is all about saving money, and that’s what implementation of the Blockchain would mean for these big banks, let alone the fact that Blockchain transactions are quicker and more secure as well. These new technologies could transform how financial transactions are recorded, reconciled and reported…  Senior Vice President and head of Emerging Technologies at State Street Hu Liang added,  … all with additional security, lower error rates and significant cost reductions. The step to work together on the implementation of the Blockchain in financial markets is logical as banks and other financial institutions would need to agree to standards eventually, so it makes sense that some of the big names would be involved collaboratively in developing those standards. There were no time frames given by the group in terms of targets, however in theory working together on the Blockchain could see the platform takeover financial markets even quicker than many had already expected.")

    println(text)
  }

  //
  //  "doc": {
  //    "info": {
  //    "calaisRequestID": "22bb0b11-67cd-8975-152b-0abd1ce4cd86",
  //    "id": "http://id.opencalais.com/45-X*FzVGh*8grJsbg*Mxg",
  //    "ontology": "http://mdaast-virtual-onecalais.int.thomsonreuters.com/owlschema/9.2/onecalais.owl.allmetadata.xml",
  //    "docId": "http://d.opencalais.com/dochash-1/48ed88ce-7b4b-3fb3-80a4-038019040002",
  //    "document": "Nine major banks have joined with fintech startup R3CEV LLC (R3) to develop a framework for using Blockchain technology in financial markets. The announcement of the partnership to design and deliver advanced distributed/shared ledger technologies to global financial markets includes some of the biggest names in global banking, including Barclays, BBVA, Commonwealth Bank of Australia (CBA), Credit Suisse, J.P. Morgan, State Street, Royal Bank of Scotland and UBS. Utilizing the Blockchain, the digital ledger that underpins Bitcoin, the group plans to collaborate on research, experimentation, design, and engineering with an aim of delivering advanced state-of-the-art enterprise-scale shared ledger solutions that also meet banking requirements for security, reliability, performance, scalability, and audit. Led by R3, the group will establish collaborative joint working groups to develop these efforts; many, if not all of its members have previously worked on ways of using the Blockchain in everyday business, with the likes of the CBA working on Blockchain financial settlements, and Barclays even going as far as accepting Bitcoin payments. Kevin Hanley, Director of Design at Royal Bank of Scotland explained why the group is coming together, saying in a statement  Right now you’re seeing significant money and time being spent on exploration of these technologies in a fractured way that lacks the strategic, coordinated vision so critical to timely success. The R3 model is changing the game. The collaborative model we’ve established with R3 and the other banks is a very effective way to deliver robust shared ledger solutions to the financial services sector,  he said. Saving money. Cutting out the middleman in financial transactions is all about saving money, and that’s what implementation of the Blockchain would mean for these big banks, let alone the fact that Blockchain transactions are quicker and more secure as well. These new technologies could transform how financial transactions are recorded, reconciled and reported…  Senior Vice President and head of Emerging Technologies at State Street Hu Liang added,  … all with additional security, lower error rates and significant cost reductions. The step to work together on the implementation of the Blockchain in financial markets is logical as banks and other financial institutions would need to agree to standards eventually, so it makes sense that some of the big names would be involved collaboratively in developing those standards. There were no time frames given by the group in terms of targets, however in theory working together on the Blockchain could see the platform takeover financial markets even quicker than many had already expected.",
  //    "docTitle": "",
  //    "docDate": "2016-02-05 09:03:16.917"
  //  },
  //    "meta": {
  //    "contentType": "text/raw",
  //    "processingVer": "AllMetadata",
  //    "serverVersion": "OneCalais_9.2-RELEASE:402",
  //    "stagsVer": "OneCalais_9.2-RELEASE-b2-2015-12-31_01:34:35",
  //    "submissionDate": "2016-02-05 09:03:16.430",
  //    "submitterCode": "0ca6a864-5659-789d-5f32-f365f695e757",
  //    "signature": "digestalg-1|nkgRh9eA6txGddgGUWoknqY02mA=|UbHRc0tfZRYA6Z1OrejLJzXpb3YeDfSuhEPdq63Xsm0RMex2xvZEiQ==",
  //    "language": "English"
  //  }
  //  }

  case class OpenCalaisDocInfo(calaisRequestID:String,id:String,ontology:String,docId:String,document:String,docTitle:String,docDate:String)
  case class OpenCalaisDocMeta(contentType:String,processingVer:String,serverVersion:String,stagsVer:String,submissionDate:String,submitterCode:String,signature:String,language:String)
  case class OpenCalaisDoc(info:OpenCalaisDocInfo,meta:OpenCalaisDocMeta)


  case class OpenCalaisEntityInstance(
                                       detection:Option[String],
                                       prefix:Option[String],
                                       exact:String,
                                       suffix:Option[String],
                                       offset:Option[Int],
                                       length:Option[Int])

  case class OpenCalaisEntity(
                               _typeGroup:String,
                               _type:String,
                               forenduserdisplay:Option[String],
                               name:String,
                               nationality:Option[String],
                               confidencelevel:Option[String],
                               _typeReference:Option[String],
                               instances:List[OpenCalaisEntityInstance]
                             )

  implicit val openCalaisDocInfoFormat = jsonFormat7(OpenCalaisDocInfo)
  implicit val openCalaisMetaInfoFormat = jsonFormat8(OpenCalaisDocMeta)
  implicit val openCalaisEntityInstaceFormat = jsonFormat6(OpenCalaisEntityInstance)
  implicit val openCalaisDocFormat: RootJsonFormat[OpenCalaisDoc] = jsonFormat2(OpenCalaisDoc)
  val uuopenCalaisDocFormat = jsonFormat2(OpenCalaisDoc)
  implicit val openCalaisEntityFormat = jsonFormat8(OpenCalaisEntity)

  case class OpenCalaisResponse(doc:OpenCalaisDoc,entities:List[OpenCalaisEntity])

  implicit object OpenCalaisResponseFormat extends RootJsonFormat[OpenCalaisResponse] {

    def write(c: OpenCalaisResponse) =
     //FIXME no need to read
      JsObject(

      )

    def read(value: JsValue) = {

      val jsObject = value.asJsObject
      val doc: JsObject = jsObject.getFields("doc")(0).asJsObject

      val unMarshalledDoc = doc.convertTo[OpenCalaisDoc]

      val entities: Iterable[JsValue] = jsObject.fields.values.filter(z=>{
        val types = z.asJsObject.getFields("_typeGroup")
        !types.isEmpty &&
        types(0).toString().equals("\"entities\"")
      })

      val entities1: Iterable[OpenCalaisEntity] = entities map { _.asJsObject.convertTo[OpenCalaisEntity]}

      OpenCalaisResponse(unMarshalledDoc,entities1.toList)

    }
  }

  //  "http://d.opencalais.com/comphash-1/e2e9a778-b5b1-35b8-8f67-8d310c83402d": {
  //    "_typeGroup": "entities",
//    "_type": "Company",
//    "forenduserdisplay": "false",
//    "name": "State Street",
//    "nationality": "N/A",
//    "confidencelevel": "0.934",
//    "_typeReference": "http://s.opencalais.com/1/type/em/e/Company",
//    "instances": [
//  {
//    "detection": "[of Australia (CBA), Credit Suisse, J.P. Morgan, ]State Street[, Royal Bank of Scotland and UBS. Utilizing the]",
//    "prefix": "of Australia (CBA), Credit Suisse, J.P. Morgan, ",
//    "exact": "State Street",
//    "suffix": ", Royal Bank of Scotland and UBS. Utilizing the",
//    "offset": 422,
//    "length": 12
//  },
//  {
//    "detection": "[President and head of Emerging Technologies at ]State Street[ Hu Liang added,  … all with additional security,]",
//    "prefix": "President and head of Emerging Technologies at ",
//    "exact": "State Street",
//    "suffix": " Hu Liang added,  … all with additional security,",
//    "offset": 2114,
//    "length": 12
//  }
//    ],
//    "relevance": 0.2,
//    "resolutions": [
//  {
//    "name": "STATE STREET CORPORATION",
//    "permid": "4295904976",
//    "primaryric": "STT.N",
//    "commonname": "State Str",
//    "score": 0.86862904,
//    "id": "https://permid.org/1-4295904976",
//    "ticker": "STT"
//  }
//    ],
//    "confidence": {
//    "statisticalfeature": "0.914",
//    "dblookup": "0.0",
//    "resolution": "0.86862904",
//    "aggregate": "0.934"
//  }
//  }

  override def extractEntitiesFromText(text: String): Seq[Entity] = {

    val request: HttpRequest = HttpRequest(uri = url,
      method = HttpMethods.POST,
      headers = List(RawHeader("x-ag-access-token",alchemiApi),
        RawHeader("outputFormat","application/json")),
      entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`,
        text))


    val call: Future[HttpResponse] = executeHttpsCall(request)

    val result: HttpResponse = Await.result(call, 30 seconds)

    val contentType: String = Await.result(result.entity.toStrict(30 seconds).map(_.data.decodeString("UTF-8")),30 seconds)

    val result1 = contentType.toString.parseJson.convertTo[OpenCalaisResponse]


    result1.entities.flatMap(x => {
      Entity(x.name, entityTypesMap.getOrElse(x._type,"")) :: x.instances.map(y => Entity(y.exact, entityTypesMap.getOrElse(x._type,"")))
    })
      .filter(!_.category.equals(""))



  }

}

