#!/usr/bin/env bash
export CLASSPATH=$(PWD)/../target/scala-2.11/corenlp-assembly-1.0.0-SNAPSHOT.jar
export PROPS=$(PWD)/../src/main/resources/patterns/example.properties
java -cp $CLASSPATH edu.stanford.nlp.patterns.GetPatternsFromDataMultiClass -props $PROPS