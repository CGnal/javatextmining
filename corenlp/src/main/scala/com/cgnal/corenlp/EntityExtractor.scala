package com.cgnal.corenlp

import java.net.URL
import java.nio.file.{Files, Paths}
import java.util

import edu.stanford.nlp.simple.{Sentence, _}

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer;

/**
  *
  */
trait EntityExtractor extends NamedEntityExtractor{


  def readWholeFileToString(resource: URL): String = {
    new String(Files.readAllBytes(Paths.get(resource.toURI.getPath)))
  }

  def extractEntities2(sentence: Sentence): util.List[String] = {
    val nerTags = sentence.nerTags();
    val indices: Set[Int] = nerTags
      .zipWithIndex
      .collect(
        { case ("PERSON" | "ORGANIZATION" | "LOCATION" | "DATE", i) => i }).toSet

    sentence.lemmas().zipWithIndex.filter(n => {
      indices.contains(n._2)
    }).unzip._1
  }

  val allRegexp = List("""\s*,\s*""".r)

  val entityTypes = Set("ORGANIZATION", "LOCATION", "PERSON", "DATE")

  def extractEntitiesFromSentence(sentence: Sentence): ArrayBuffer[(String, String)] = {

    val nerTags = sentence.nerTags()
    val lemmas = sentence.lemmas()

    val entitiesTagged: Array[(String, Int)] = nerTags
      .zipWithIndex
      //.filter(w => entityTypes.contains(w._1))
      .filter(!_._1.equals("O"))
      .toArray


    val buffer = ArrayBuffer[(String, ArrayBuffer[Int])]()

    for (i <- 0 to entitiesTagged.length - 1) {

      val previous: (String, Int) = if (i == 0) ("", 0) else entitiesTagged(i - 1)
      val current: (String, Int) = entitiesTagged(i)

      if (
        previous._1.equals(current._1) &&
          (previous._2 + 1).equals(current._2) &&
          (previous._2 + 1).equals(current._2) &&
          allRegexp.forall(m => m.findAllIn(sentence.lemma(current._2)).isEmpty)
      ) {
        buffer.last._2 += current._2
      }
      else buffer += ((current._1, ArrayBuffer(current._2)))
    }

    buffer.map(x => (x._1, x._2.map(i => sentence.lemma(i)).mkString(" ")))
  }

  def extractEntities(text: String): Seq[(String, String)] = {
    val extracted: mutable.Buffer[ArrayBuffer[(String, String)]] = for {
      sent <- new Document(text).sentences()
    } yield extractEntitiesFromSentence(sent)

    extracted.filter(!_.isEmpty).flatten
  }

  def extractEntitiesFromText(text: String): Seq[Entity] = {
    val extracted: mutable.Buffer[ArrayBuffer[(String, String)]] = for {
      sentence <- new Document(text).sentences()
    } yield extractEntitiesFromSentence(sentence)

    extracted.filter(!_.isEmpty).flatten.map(x => {
      Entity(x._2, x._1)
    })
  }
}
