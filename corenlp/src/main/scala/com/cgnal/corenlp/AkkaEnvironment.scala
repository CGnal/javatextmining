package com.cgnal.corenlp

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializerSettings, ActorMaterializer}
import com.cgnal.corenlp.RosetteEntityExtractor._

/**
  *
  */
trait AkkaEnvironment {

  implicit val system = ActorSystem()
  implicit val materializer =
    ActorMaterializer(ActorMaterializerSettings(system)
      .withInputBuffer(initialSize = 16384, maxSize = 16384*8))




}
