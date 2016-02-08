import sbt._

fork := true

val versionLingPipe: String = "4.1.0"
val versionScalaLogging: String = "3.1.0"
val versionScalaReflect: String = "2.11.7"
val slf4jVersion: String = "1.7.12"
val versionLog4j: String = "2.4.1"
val versionScalaTest: String = "2.2.5"
val versionAkkaHttp:String = "2.0.3"

val dependenciesLingPipe = Seq(
  "com.aliasi" % "lingpipe" % "4.1.0"
)

val dependenciesOpenNlp = Seq(
  "org.apache.opennlp" % "opennlp-tools" % "1.5.3"
)

val dependenciesGate = Seq(
  "uk.ac.gate" % "gate-core" % "8.1"
)

val scalanlpDependencies = Seq(
   "org.scalanlp" %% "epic" % "0.4-SNAPSHOT"
)

val akkaDependencies = Seq (
  "com.typesafe.akka"          %% "akka-stream-experimental"          % versionAkkaHttp,
  "com.typesafe.akka"          %% "akka-http-spray-json-experimental" % versionAkkaHttp
)

val coreNlpDependencies = Seq(
  "edu.stanford.nlp" % "stanford-corenlp" % "3.6.0",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.6.0" classifier "models",
  "com.google.protobuf" % "protobuf-java" % "2.6.1",
  "org.glassfish" % "javax.json" % "1.0.4"
)


def commonSettings(moduleName: String) = Seq(
  name := moduleName,
  organization := "com.cgnal",
  version := "1.0.0-SNAPSHOT",
  scalaVersion := versionScalaReflect,
  javaOptions += "-Xms512m -Xmx2G",
  resolvers ++= Seq(Resolver.mavenLocal, "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
    "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"),
libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % versionScalaLogging,
  "com.typesafe" % "config" % "1.2.1",
  "org.slf4j" % "slf4j-api" % slf4jVersion,
  "org.slf4j" % "slf4j-simple" % slf4jVersion ,
  /*"org.apache.logging.log4j" % "log4j-slf4j-impl" % versionLog4j,
  "org.apache.logging.log4j" % "log4j-api" % versionLog4j,
  "org.apache.logging.log4j" % "log4j-core" % versionLog4j,*/
  "org.scalatest" %% "scalatest" % versionScalaTest % "test"
)
)

lazy val javatextmining = (project in file(".")).aggregate(
  lingpipe,
  opennlptest,
  gate,
  scalaNLP,
  corenlptest,
  nlpservicesevaluator
)

lazy val gate = (project in file("gate")).
  settings(commonSettings("gate"): _*).
  settings(
    libraryDependencies ++= dependenciesGate
  )

lazy val lingpipe = (project in file("lingpipe")).
  settings(commonSettings("lingpipe"): _*).
  settings(
    libraryDependencies ++= dependenciesLingPipe
  )

lazy val scalaNLP = (project in file("scalanlp")).
  settings(commonSettings("scalanlp"): _*).
  settings(
    libraryDependencies ++= scalanlpDependencies
  )


lazy val opennlptest = (project in file("opennlptest")).
  settings(commonSettings("opennlptest"): _*).
  settings(
    libraryDependencies ++= dependenciesOpenNlp
  )

lazy val corenlptest = (project in file("corenlp")).
  settings(commonSettings("corenlp"): _*).
  settings(
    libraryDependencies ++= coreNlpDependencies,
    libraryDependencies ++= akkaDependencies
  )

lazy val nlpservicesevaluator = (project in file("nlpservicesevaluator")).
  settings(commonSettings("nlpservicesevaluator"): _*).
  settings(
    libraryDependencies ++= akkaDependencies
  )
//.dependsOn(core)
