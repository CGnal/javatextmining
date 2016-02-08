package com.cgnal

import java.net.URL
import java.nio.file.{Paths, Files}

import epic.preprocess.MLSentenceSegmenter
import epic.sequences.SemiCRF
import epic.trees.AnnotatedLabel

/**
  *
  */
object ScalaNER {

  def readWholeFileToString(resource: URL): String = {
    new String(Files.readAllBytes(Paths.get(resource.toURI.getPath)))
  }

  def main(args: Array[String]) {

    val ner = epic.models.NerSelector.loadNer("en").get// or another 2 letter code.
    //var path = "model.ser.gz"
   // val ner: SemiCRF[Any, String] = EnglishConllNer.load();

  //  epic.parser.models.en.span.EnglishSpanParser.load()

    val text = readWholeFileToString(ScalaNER.getClass().getResource("prova.txt"))

    val sentenceSplitter = MLSentenceSegmenter.bundled().get
    val tokenizer = new epic.preprocess.TreebankTokenizer()

    val sentences: IndexedSeq[IndexedSeq[String]] = sentenceSplitter(text).map(tokenizer).toIndexedSeq

    for(sentence <- sentences) {
      // use the sentence tokens
      val segments = ner.bestSequence(sentence)
      println(segments.render)
    }



  }

}
