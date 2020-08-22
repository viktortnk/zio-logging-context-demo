import sbt._

object Dependencies {

  object Version {
    val catsEffect = "2.1.4"

    val logback = "1.2.3"

    val zio = "1.0.1"

    val zioCats = "2.1.4.0"

    val log4Cats = "1.1.1"

    val simulacrum = "1.0.0"

    val kindProjector = "0.11.0"

    val betterMonadicFor = "0.3.1"
  }

  val catsEffect = "org.typelevel" %% "cats-effect" % Version.catsEffect
  val logback = "ch.qos.logback" % "logback-classic" % Version.logback
  val zio = "dev.zio" %% "zio" % Version.zio
  val zioCats = "dev.zio" %% "zio-interop-cats" % Version.zioCats
  val log4Cats = "io.chrisdavenport" %% "log4cats-core" % Version.log4Cats

  val kindProjector = "org.typelevel" %% "kind-projector" % Version.kindProjector cross CrossVersion.patch
  val simulacrum = "org.typelevel" %% "simulacrum" % Version.simulacrum
  val betterMonadicFor = "com.olegpy" %% "better-monadic-for" % Version.betterMonadicFor
}
