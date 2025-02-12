ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.8"

lazy val root = (project in file("."))
  .settings(
    name := "OKN_MATCHBOX"
  )
//assemblyMergeStrategy in assembly := {
//  case PathList("META-INF", _*) => MergeStrategy.discard
 // case _                        => MergeStrategy.first
//}

libraryDependencies += "org.apache.spark" % "spark-core_2.12" % "3.5.1" //% "provided"
libraryDependencies += "org.apache.spark" % "spark-sql_2.12" % "3.5.1" //% "provided"
//libraryDependencies += "com.squareup.okhttp3" % "okhttp" % "4.12.0"
//libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.4.2"
libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.5.14"
//libraryDependencies ++= Seq(
//  "com.softwaremill.sttp.client3" %% "core" % "3.8.3", // sttp client core
//  "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats" % "3.8.3" // backend for async http client
//)
libraryDependencies += "com.softwaremill.sttp.client4" %% "core" % "4.0.0-M22"
libraryDependencies += "org.postgresql" % "postgresql" % "42.7.2" //% "provided"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.9.2"



assembly / assemblyOutputPath := baseDirectory.value / "target" / "my-app.jar"

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case "application.conf"            => MergeStrategy.concat
  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}