package com.cgnal.corenlp

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}

/**
  *
  */
object WebserverMain extends App {

   implicit val system = ActorSystem()
   implicit val materializer =
      ActorMaterializer(ActorMaterializerSettings(system)
        .withInputBuffer(initialSize = 16384, maxSize = 16384*8))
    implicit val execContext = system.dispatcher

  private val server = new WebServer()
  server.start()


}
