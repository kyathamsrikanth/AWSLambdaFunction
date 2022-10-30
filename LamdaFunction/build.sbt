ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.6"

lazy val root = (project in file("."))
  .settings(
    name := "LamdaFunction"

  )

val logbackVersion = "1.3.0-alpha10"
val sfl4sVersion = "2.0.0-alpha5"
val typesafeConfigVersion = "1.4.1"
val apacheCommonIOVersion = "2.11.0"
val awsVersion = "2.17.66"
val scalacticVersion = "3.2.9"
val generexVersion = "1.0.2"
libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-core" % logbackVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "org.slf4j" % "slf4j-api" % sfl4sVersion,
  "com.typesafe" % "config" % typesafeConfigVersion,
  "commons-io" % "commons-io" % apacheCommonIOVersion,
  "org.scalactic" %% "scalactic" % scalacticVersion,
  "org.scalatest" %% "scalatest" % scalacticVersion % Test,
  "org.scalatest" %% "scalatest-featurespec" % scalacticVersion % Test,
  "com.typesafe" % "config" % typesafeConfigVersion,
  "com.github.mifmif" % "generex" % generexVersion,
  "software.amazon.awssdk" % "s3" % awsVersion,
  "software.amazon.awssdk" % "lambda" % awsVersion,
  "com.amazonaws" % "aws-lambda-java-core" % "1.2.1",
  "com.amazonaws" % "aws-lambda-java-events" % "3.10.0",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.13.0",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.0",
  "org.json4s" %% "json4s-jackson" % "4.0.3",
  "com.thesamet.scalapb" %% "scalapb-json4s" % "0.12.0",
  "com.typesafe.play" %% "play-json" % "2.9.2"
)
assembly / assemblyMergeStrategy:= {
  case PathList("META-INF", _*) => MergeStrategy.discard
  case _ => MergeStrategy.first
}