import sbt.Keys._

// Common dependency versions
val akkaVersion = "2.4.16"
val springVersion = "4.3.6.RELEASE"
val springBootVersion = "1.4.4.RELEASE"

lazy val `akka-spring-boot` = (project in file(".")).
  settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*).
  settings(
    organization        := "com.github.scalaspring",
    name                := "akka-spring-boot",
    description         := "Scala-based integration of Akka with Spring Boot.\nTwo-way Akka<->Spring configuration bindings and convention over configuration with sensible automatic defaults get your project running quickly.",
    scalaVersion        := "2.11.8",
    crossScalaVersions  := Seq("2.10.6"),
    javacOptions        := Seq("-source", "1.7", "-target", "1.7"),
    scalacOptions       ++= Seq("-feature", "-deprecation"),
    resolvers           += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    libraryDependencies ++= Seq(
      "com.typesafe.scala-logging" %% "scala-logging" % "3.+",
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "org.springframework" % "spring-context" % springVersion,
      "org.springframework.boot" % "spring-boot-starter" % springBootVersion
      // The following dependencies support configuration validation
      //"javax.validation" % "validation-api" % "1.1.0.Final",
      //"javax.el" % "javax.el-api" % "3.0.1-b04",
      //"org.hibernate" % "hibernate-validator" % "5.1.3.Final",
    ),
    // Runtime dependencies
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.1.2"
    ).map { _ % "runtime" },
    // Test dependencies
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.1",
      "com.github.scalaspring" %% "scalatest-spring" % "0.3.1",
      "org.springframework" % "spring-test" % springVersion,
      "org.springframework.boot" % "spring-boot-starter-test" % springBootVersion,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion
    ).map { _ % "test" },
    // Publishing settings
    publishMavenStyle       := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },
    pomExtra :=
      <url>http://github.com/scalaspring/akka-spring-boot</url>
      <licenses>
        <license>
          <name>Apache License, Version 2.0</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:scalaspring/akka-spring-boot.git</url>
        <connection>scm:git:git@github.com:scalaspring/akka-spring-boot.git</connection>
      </scm>
      <developers>
        <developer>
          <id>lancearlaus</id>
          <name>Lance Arlaus</name>
          <url>http://lancearlaus.github.com</url>
        </developer>
      </developers>
  )
