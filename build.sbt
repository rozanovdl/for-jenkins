ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.8"

lazy val root = (project in file("."))
  .settings(
    name := "OKN_MATCHBOX",
    assembly / assemblyMergeStrategy:=  {
      case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
      case PathList("reference.conf") => MergeStrategy.concat
      case PathList("META-INF", "services", _*) => MergeStrategy.concat
      case PathList("META-INF", _*) => MergeStrategy.discard
      case _ => MergeStrategy.first
    }
  )

libraryDependencies += "org.apache.spark" % "spark-core_2.12" % "3.5.1" //% "provided"
libraryDependencies += "org.apache.spark" % "spark-sql_2.12" % "3.5.1" //% "provided"
libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.5.14"
libraryDependencies += "com.softwaremill.sttp.client4" %% "core" % "4.0.0-M22"
libraryDependencies += "org.postgresql" % "postgresql" % "42.7.2" //% "provided"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.9.2"




