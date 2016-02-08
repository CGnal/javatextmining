package com.cgnal.corenlp

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source, Flow}

import scala.concurrent.{ExecutionContext, Future}

/**
  *
  */
trait HttpClient extends AkkaEnvironment {

  lazy val connectionFlow: Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]] =
    Http().outgoingConnection(host, port)

  lazy val httpsConnectionFlow: Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]] =
    Http().outgoingConnectionTls(host, port)

  def executeCall(request: HttpRequest) = {
    Source.single(request)
      .via(connectionFlow)
      .runWith(Sink.head)
  }

  def executeHttpsCall(request: HttpRequest) = {
    Source.single(request)
      .via(httpsConnectionFlow)
      .runWith(Sink.head)
  }

  val host:String
  val port:Int


}
