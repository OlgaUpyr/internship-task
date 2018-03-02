name := """Internship"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

resolvers += Resolver.sonatypeRepo("snapshots")

scalaVersion := "2.12.4"

crossScalaVersions := Seq("2.11.12", "2.12.4")

libraryDependencies += jdbc
libraryDependencies += guice
libraryDependencies += ws
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies += "com.h2database" % "h2" % "1.4.196"
libraryDependencies += "org.postgresql" % "postgresql" % "9.4-1206-jdbc42"
libraryDependencies += "com.adrianhurt" %% "play-bootstrap" % "1.2-P26-B3"
libraryDependencies += "javax.xml.bind" % "jaxb-api" % "2.1"
libraryDependencies += "com.typesafe.play" %% "anorm" % "2.5.3"
libraryDependencies += "com.typesafe.play" %% "play-mailer" % "6.0.0"
libraryDependencies += "com.typesafe.play" %% "play-mailer-guice" % "6.0.0"
libraryDependencies += "com.novocode" % "junit-interface" % "0.9" % "test->default"
libraryDependencies += "junit" % "junit" % "4.11" % "test"
libraryDependencies += "org.mockito" % "mockito-core" % "1.9.5"
libraryDependencies += "com.amazonaws" % "aws-java-sdk" % "1.3.11"
libraryDependencies += "org.im4java" % "im4java" % "1.4.0"
libraryDependencies += "org.skyscreamer" % "jsonassert" % "1.5.0" % Test