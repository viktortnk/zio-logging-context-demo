package com.whisk.util.logging

import cats.effect.Bracket
import cats.syntax.flatMap._
import cats.syntax.functor._

trait LoggingContext[F[_]] {
  type Entry = Map[String, String]

  def get: F[Entry]

  def set(a: Entry): F[Unit]

  def getAndSet(a: Entry): F[Entry]

  def update(f: Entry => Entry): F[Unit]

  def withLocal[A, E](value: Map[String, String])(use: F[A])(implicit B: Bracket[F, E]): F[A] =
    for {
      oldValue <- get
      b <- B.bracket(set(oldValue ++ value))(_ => use)(_ => set(oldValue))
    } yield b
}

object LoggingContext {

  def apply[F[_]: LoggingContext]: LoggingContext[F] = implicitly[LoggingContext[F]]
}
