package com.cgnal.corenlp

/**
  *
  */
trait NamedEntityExtractor {

  def extractEntitiesFromText(text: String): Seq[Entity]

}
