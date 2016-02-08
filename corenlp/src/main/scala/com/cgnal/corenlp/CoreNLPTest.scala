package com.cgnal.corenlp;

/**
  *
  */
object CoreNLPTest extends App with EntityExtractor {

  println(extractEntities("Mr Swanson will be in Sydney this week to address the Sydney Blockchain Workshops, which are being supported by the Commonwealth Bank of Australia."))
  val text = readWholeFileToString(CoreNLPTest.getClass().getResource("/prova.txt"))
  println(extractEntities(text))


}
