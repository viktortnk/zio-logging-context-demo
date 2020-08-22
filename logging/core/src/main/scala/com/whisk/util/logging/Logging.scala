package com.whisk.util.logging

import cats.FlatMap
import cats.effect.Sync
import cats.syntax.functor._
import cats.syntax.flatMap._
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

trait Logging[F[_]] {

  def fromClass(cls: Class[_]): F[Logger[F]]

  def fromName(name: String): F[Logger[F]]

  def getLoggerFromClass(clazz: Class[_]): Logger[F]

  def getLoggerFromName(name: String): Logger[F]
}

trait MDCLogging[F[_]] extends Logging[F] with LoggingContext[F]

object Logging {

  def apply[F[_]: Logging]: Logging[F] = implicitly[Logging[F]]

  def noContext[F[_]: Sync]: Logging[F] =
    new Logging[F] {
      override def fromClass(cls: Class[_]): F[Logger[F]] =
        Slf4jLogger.fromClass[F](cls).widen[Logger[F]]

      override def fromName(name: String): F[Logger[F]] =
        Slf4jLogger.fromName[F](name).widen[Logger[F]]

      override def getLoggerFromClass(clazz: Class[_]): Logger[F] =
        Slf4jLogger.getLoggerFromClass[F](clazz)

      override def getLoggerFromName(name: String): Logger[F] =
        Slf4jLogger.getLoggerFromName[F](name)
    }

  def withContext[F[_]: Sync](implicit LC: LoggingContext[F]): MDCLogging[F] = {
    new MDCLogging[F] {
      override def fromClass(cls: Class[_]): F[Logger[F]] =
        Slf4jLogger
          .fromClass[F](cls)
          .widen[Logger[F]]
          .map(new ContextAwareLogging(_, LC))

      override def fromName(name: String): F[Logger[F]] =
        Slf4jLogger
          .fromName[F](name)
          .widen[Logger[F]]
          .map(new ContextAwareLogging(_, LC))

      override def getLoggerFromClass(clazz: Class[_]): Logger[F] =
        new ContextAwareLogging(Slf4jLogger.getLoggerFromClass[F](clazz), LC)

      override def getLoggerFromName(name: String): Logger[F] =
        new ContextAwareLogging(Slf4jLogger.getLoggerFromName[F](name), LC)

      override def get: F[Entry] = LC.get

      override def set(a: Entry): F[Unit] = LC.set(a)

      override def getAndSet(a: Entry): F[Entry] = LC.getAndSet(a)

      override def update(f: Entry => Entry): F[Unit] = LC.update(f)
    }
  }

  private class ContextAwareLogging[F[_]: FlatMap](log: Logger[F], lc: LoggingContext[F]) extends Logger[F] {

    private def withOuter(func: lc.Entry => F[Unit]): F[Unit] =
      lc.get.flatMap(func)

    def error(message: => String): F[Unit] = withOuter(log.error(_)(message))
    def warn(message: => String): F[Unit] = withOuter(log.warn(_)(message))
    def info(message: => String): F[Unit] = withOuter(log.info(_)(message))
    def debug(message: => String): F[Unit] = withOuter(log.debug(_)(message))
    def trace(message: => String): F[Unit] = withOuter(log.trace(_)(message))

    def trace(ctx: Map[String, String])(msg: => String): F[Unit] =
      withOuter(outer => log.trace(outer ++ ctx)(msg))
    def debug(ctx: Map[String, String])(msg: => String): F[Unit] =
      withOuter(outer => log.debug(outer ++ ctx)(msg))
    def info(ctx: Map[String, String])(msg: => String): F[Unit] =
      withOuter(outer => log.info(outer ++ ctx)(msg))
    def warn(ctx: Map[String, String])(msg: => String): F[Unit] =
      withOuter(outer => log.warn(outer ++ ctx)(msg))
    def error(ctx: Map[String, String])(msg: => String): F[Unit] =
      withOuter(outer => log.error(outer ++ ctx)(msg))

    def error(t: Throwable)(message: => String): F[Unit] =
      withOuter(outer => log.error(outer, t)(message))
    def warn(t: Throwable)(message: => String): F[Unit] =
      withOuter(outer => log.warn(outer, t)(message))
    def info(t: Throwable)(message: => String): F[Unit] =
      withOuter(outer => log.info(outer, t)(message))
    def debug(t: Throwable)(message: => String): F[Unit] =
      withOuter(outer => log.debug(outer, t)(message))
    def trace(t: Throwable)(message: => String): F[Unit] =
      withOuter(outer => log.trace(outer, t)(message))

    def error(ctx: Map[String, String], t: Throwable)(message: => String): F[Unit] =
      withOuter(outer => log.error(outer ++ ctx, t)(message))
    def warn(ctx: Map[String, String], t: Throwable)(message: => String): F[Unit] =
      withOuter(outer => log.warn(outer ++ ctx, t)(message))
    def info(ctx: Map[String, String], t: Throwable)(message: => String): F[Unit] =
      withOuter(outer => log.info(outer ++ ctx, t)(message))
    def debug(ctx: Map[String, String], t: Throwable)(message: => String): F[Unit] =
      withOuter(outer => log.debug(outer ++ ctx, t)(message))
    def trace(ctx: Map[String, String], t: Throwable)(message: => String): F[Unit] =
      withOuter(outer => log.trace(outer ++ ctx, t)(message))
  }
}
