package com.cgnal.corenlp

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import scala.concurrent.ExecutionContext

/**
  *
  */
trait AkkaEnvironment {

  protected[this] implicit def system: ActorSystem
  protected[this] implicit def materializer: ActorMaterializer
  protected[this] implicit def execContext: ExecutionContext

}
