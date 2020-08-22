import Dependencies._

version := "0.1"

val scalaV = "2.13.3"

ThisBuild / organization := "com.whisk"

lazy val defaultSettings = Seq(
  scalaVersion := scalaV,
  scalacOptions ++= Seq(
    "-deprecation", // Emit warning and location for usages of deprecated APIs.
    "-encoding",
    "utf-8", // Specify character encoding used by source files.
    "-explaintypes", // Explain type errors in more detail.
    "-feature", // Emit warning and location for usages of features that should be imported explicitly.
    "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
    "-language:higherKinds" // Allow higher-kinded types
  ),
  libraryDependencies ++= Seq(
    compilerPlugin(kindProjector),
    compilerPlugin(betterMonadicFor)
  )
)

val zioVersion = "1.0.0"

val grpcVersion = "1.31.1"

val loggingCore =
  project.in(file("logging/core"))
    .settings(
      defaultSettings,
      name := "util-logging-core",
      libraryDependencies ++= Seq(
        log4Cats,
        catsEffect
      )
    )

val zioLogging =
  project.in(file("zio/logging"))
    .settings(
      defaultSettings,
      name := "util-logging-core",
      libraryDependencies ++= Seq(
        zio
      )
    )
    .dependsOn(loggingCore)

val example =
  project.in(file("example"))
    .settings(
      defaultSettings,
      name := "example",
      PB.targets in Compile := Seq(
        scalapb.gen(grpc = true) -> (sourceManaged in Compile).value,
        scalapb.zio_grpc.ZioCodeGenerator -> (sourceManaged in Compile).value
      ),
      libraryDependencies ++= Seq(
        "io.grpc" % "grpc-netty" % grpcVersion,
        "io.netty" % "netty-tcnative-boringssl-static" % "2.0.34.Final",
        "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
        "com.thesamet.scalapb" %% "scalapb-json4s" % "0.10.1",
        "com.linecorp.armeria" % "armeria-grpc" % "0.99.9",
        "com.linecorp.armeria" % "armeria" % "0.99.9",
        logback,
        zioCats,
        "net.logstash.logback" % "logstash-logback-encoder" % "6.4",
        "io.chrisdavenport" %% "log4cats-slf4j" % Version.log4Cats,
      ),
      run / fork := true
    ).dependsOn(loggingCore, zioLogging)
