package com.whisk.util.logging

import cats.FlatMap
import cats.syntax.flatMap._

class EmbedLogger[F[_]: FlatMap](underlying: F[Logger[F]]) extends Logger[F] {

  override def trace(ctx: Map[String, String])(msg: => String): F[Unit] =
    underlying.flatMap(_.trace(ctx)(msg))

  override def trace(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] =
    underlying.flatMap(_.trace(ctx, t)(msg))

  override def debug(ctx: Map[String, String])(msg: => String): F[Unit] =
    underlying.flatMap(_.debug(ctx)(msg))

  override def debug(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] =
    underlying.flatMap(_.debug(ctx, t)(msg))

  override def info(ctx: Map[String, String])(msg: => String): F[Unit] =
    underlying.flatMap(_.info(ctx)(msg))

  override def info(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] =
    underlying.flatMap(_.info(ctx, t)(msg))

  override def warn(ctx: Map[String, String])(msg: => String): F[Unit] =
    underlying.flatMap(_.warn(ctx)(msg))

  override def warn(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] =
    underlying.flatMap(_.warn(ctx, t)(msg))

  override def error(ctx: Map[String, String])(msg: => String): F[Unit] =
    underlying.flatMap(_.error(ctx)(msg))

  override def error(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] =
    underlying.flatMap(_.error(ctx, t)(msg))

  override def error(message: => String): F[Unit] =
    underlying.flatMap(_.error(message))

  override def warn(message: => String): F[Unit] =
    underlying.flatMap(_.warn(message))

  override def info(message: => String): F[Unit] =
    underlying.flatMap(_.info(message))

  override def debug(message: => String): F[Unit] =
    underlying.flatMap(_.debug(message))

  override def trace(message: => String): F[Unit] =
    underlying.flatMap(_.trace(message))

  override def error(t: Throwable)(message: => String): F[Unit] =
    underlying.flatMap(_.error(t)(message))

  override def warn(t: Throwable)(message: => String): F[Unit] =
    underlying.flatMap(_.warn(t)(message))

  override def info(t: Throwable)(message: => String): F[Unit] =
    underlying.flatMap(_.info(t)(message))

  override def debug(t: Throwable)(message: => String): F[Unit] =
    underlying.flatMap(_.debug(t)(message))

  override def trace(t: Throwable)(message: => String): F[Unit] =
    underlying.flatMap(_.trace(t)(message))
}
