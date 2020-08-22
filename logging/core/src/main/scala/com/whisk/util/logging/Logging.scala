package com.whisk.util.logging

import cats.FlatMap
import cats.effect.Sync
import cats.syntax.functor._
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

trait Logging[F[_]] extends Logger[F] {

  def getLogger(implicit f: Sync[F]): Logger[F]

  def getLoggerFromName(name: String)(implicit f: Sync[F]): Logger[F]

  def create(implicit f: Sync[F]): F[Logger[F]]
}

trait MDCLogging[F[_]] extends Logging[F] with LoggingContext[F]

object Logging {

  def apply[F[_]: Logging]: Logging[F] = implicitly[Logging[F]]

  def createWithContext[F[_]: Sync](implicit LC: LoggingContext[F]): F[MDCLogging[F]] = {
    for {
      logger <- Slf4jLogger.fromName[F]("default")
    } yield {
      new ContextAwareLogging[F](logger, LC)
    }
  }

  private class ContextAwareLogging[F[_]: FlatMap](rootLogger: Logger[F], lc: LoggingContext[F])
      extends MDCLogging[F] {
    val outer = Map.empty[String, String] //TODO fix later

    private def withOuter(func: Map[String, String] => F[Unit]): F[Unit] = {
      FlatMap[F].flatMap(lc.get)(func)
    }

    def error(message: => String): F[Unit] = withOuter(rootLogger.error(_)(message))
    def warn(message: => String): F[Unit] = withOuter(rootLogger.warn(_)(message))
    def info(message: => String): F[Unit] = withOuter(rootLogger.info(_)(message))
    def debug(message: => String): F[Unit] = withOuter(rootLogger.debug(_)(message))
    def trace(message: => String): F[Unit] = withOuter(rootLogger.trace(_)(message))

    def trace(ctx: Map[String, String])(msg: => String): F[Unit] =
      rootLogger.trace(outer ++ ctx)(msg)
    def debug(ctx: Map[String, String])(msg: => String): F[Unit] =
      rootLogger.debug(outer ++ ctx)(msg)
    def info(ctx: Map[String, String])(msg: => String): F[Unit] =
      withOuter(outer => rootLogger.info(outer ++ ctx)(msg))
    def warn(ctx: Map[String, String])(msg: => String): F[Unit] =
      rootLogger.warn(outer ++ ctx)(msg)
    def error(ctx: Map[String, String])(msg: => String): F[Unit] =
      rootLogger.error(outer ++ ctx)(msg)

    def error(t: Throwable)(message: => String): F[Unit] =
      rootLogger.error(outer, t)(message)
    def warn(t: Throwable)(message: => String): F[Unit] =
      rootLogger.warn(outer, t)(message)
    def info(t: Throwable)(message: => String): F[Unit] =
      rootLogger.info(outer, t)(message)
    def debug(t: Throwable)(message: => String): F[Unit] =
      rootLogger.debug(outer, t)(message)
    def trace(t: Throwable)(message: => String): F[Unit] =
      rootLogger.trace(outer, t)(message)

    def error(ctx: Map[String, String], t: Throwable)(message: => String): F[Unit] =
      rootLogger.error(outer ++ ctx, t)(message)
    def warn(ctx: Map[String, String], t: Throwable)(message: => String): F[Unit] =
      rootLogger.warn(outer ++ ctx, t)(message)
    def info(ctx: Map[String, String], t: Throwable)(message: => String): F[Unit] =
      rootLogger.info(outer ++ ctx, t)(message)
    def debug(ctx: Map[String, String], t: Throwable)(message: => String): F[Unit] =
      rootLogger.debug(outer ++ ctx, t)(message)
    def trace(ctx: Map[String, String], t: Throwable)(message: => String): F[Unit] =
      rootLogger.trace(outer ++ ctx, t)(message)

    override def get: F[Entry] = lc.get

    override def set(a: Entry): F[Unit] = lc.set(a)

    override def getAndSet(a: Entry): F[Entry] = lc.getAndSet(a)

    override def update(f: Entry => Entry): F[Unit] = lc.update(f)

    override def getLogger(implicit f: Sync[F]): Logger[F] = {
      new ContextAwareLogging[F](Slf4jLogger.getLogger[F], lc)
    }

    override def getLoggerFromName(name: String)(implicit f: Sync[F]): Logger[F] =
      new ContextAwareLogging[F](Slf4jLogger.getLoggerFromName[F](name: String), lc)

    override def create(implicit f: Sync[F]): F[Logger[F]] =
      Slf4jLogger.create[F].map(new ContextAwareLogging[F](_, lc))
  }
}
