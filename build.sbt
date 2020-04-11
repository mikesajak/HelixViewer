name := "HelixViewer"

version := "0.1"

scalaVersion := "2.12.8"

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

resolvers += Resolver.bintrayRepo("commercetools", "maven")

// scalafx (and fxml)
// Add dependency on ScalaFX library
libraryDependencies += "org.scalafx" %% "scalafx" % "12.0.1-R17"

// Determine OS version of JavaFX binaries
lazy val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux")   => "linux"
  case n if n.startsWith("Mac")     => "mac"
  case n if n.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
}

lazy val javaFXModules = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
libraryDependencies ++= javaFXModules.map( m => "org.openjfx" % s"javafx-$m" % "12.0.1" classifier osName)

libraryDependencies ++= { // scalafx (and fxml)
  val scalafxmlVersion = "0.4"
  Seq("org.scalafx" %% "scalafxml-core-sfx8" % scalafxmlVersion,
      "org.scalafx" % "scalafxml-guice-sfx8_2.12" % scalafxmlVersion)
  // todo: cleanup %% / %
}

// guice dependency injection
libraryDependencies ++= Seq(
  "com.google.inject" % "guice" % "4.2.2",
  "net.codingwell" %% "scala-guice" % "4.2.6"
)


// logging
libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)

//libraryDependencies += "org.json4s" %% "json4s-native" % "3.6.7"
//
//libraryDependencies += "io.sphere" %% "sphere-json" % "0.11.2"

//val circeVersion = "0.13.0"
//
//libraryDependencies ++= Seq(
//  "io.circe" %% "circe-core",
//  "io.circe" %% "circe-generic",
//  "io.circe" %% "circe-parser",
//  "io.circe" %% "circe-optics"
//  ).map(_ % circeVersion)

libraryDependencies += "io.spray" %%  "spray-json" % "1.3.5"
