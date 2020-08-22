package com.whisk.util.logging

import cats.{FlatMap, Monad}
import cats.effect.Sync
import io.chrisdavenport.log4cats.StructuredLogger

object ContextLogger {

  def wrap[F[_]: FlatMap](sl: StructuredLogger[F], lc: LoggingContext[F]): StructuredLogger[F] = {
    new ExtraContextStructuredLogger[F](sl, lc)
  }

//  def getLogger[F[_]: Sync](implicit lc: LoggingContext[F]): StructuredLogger[F] = {
//    val logger = Slf4jLogger.getLogger[F]
//    new ExtraContextStructuredLogger(logger, lc)
//  }
//
//  def ctx[F[_]: Sync: Monad](implicit lc: LoggingContext[F]): F[StructuredLogger[F]] = {
//    for {
//      logger <- Slf4jLogger.create[F]
//    } yield {
//      new ExtraContextStructuredLogger(logger, lc)
//    }
//  }

  private class ExtraContextStructuredLogger[F[_]: FlatMap](sl: StructuredLogger[F], lc: LoggingContext[F])
      extends StructuredLogger[F] {
    val outer = Map.empty[String, String] //TODO fix later

    private def withOuter(func: Map[String, String] => F[Unit]): F[Unit] = {
      FlatMap[F].flatMap(lc.get)(func)
    }

    def error(message: => String): F[Unit] = withOuter(sl.error(_)(message))
    def warn(message: => String): F[Unit] = withOuter(sl.warn(_)(message))
    def info(message: => String): F[Unit] = withOuter(sl.info(_)(message))
    def debug(message: => String): F[Unit] = withOuter(sl.debug(_)(message))
    def trace(message: => String): F[Unit] = withOuter(sl.trace(_)(message))

    def trace(ctx: Map[String, String])(msg: => String): F[Unit] =
      sl.trace(outer ++ ctx)(msg)
    def debug(ctx: Map[String, String])(msg: => String): F[Unit] =
      sl.debug(outer ++ ctx)(msg)
    def info(ctx: Map[String, String])(msg: => String): F[Unit] =
      withOuter(outer => sl.info(outer ++ ctx)(msg))
    def warn(ctx: Map[String, String])(msg: => String): F[Unit] =
      sl.warn(outer ++ ctx)(msg)
    def error(ctx: Map[String, String])(msg: => String): F[Unit] =
      sl.error(outer ++ ctx)(msg)

    def error(t: Throwable)(message: => String): F[Unit] =
      sl.error(outer, t)(message)
    def warn(t: Throwable)(message: => String): F[Unit] =
      sl.warn(outer, t)(message)
    def info(t: Throwable)(message: => String): F[Unit] =
      sl.info(outer, t)(message)
    def debug(t: Throwable)(message: => String): F[Unit] =
      sl.debug(outer, t)(message)
    def trace(t: Throwable)(message: => String): F[Unit] =
      sl.trace(outer, t)(message)

    def error(ctx: Map[String, String], t: Throwable)(message: => String): F[Unit] =
      sl.error(outer ++ ctx, t)(message)
    def warn(ctx: Map[String, String], t: Throwable)(message: => String): F[Unit] =
      sl.warn(outer ++ ctx, t)(message)
    def info(ctx: Map[String, String], t: Throwable)(message: => String): F[Unit] =
      sl.info(outer ++ ctx, t)(message)
    def debug(ctx: Map[String, String], t: Throwable)(message: => String): F[Unit] =
      sl.debug(outer ++ ctx, t)(message)
    def trace(ctx: Map[String, String], t: Throwable)(message: => String): F[Unit] =
      sl.trace(outer ++ ctx, t)(message)
  }
}
