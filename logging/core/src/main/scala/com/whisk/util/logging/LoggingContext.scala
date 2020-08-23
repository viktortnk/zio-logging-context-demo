package com.whisk.util.logging

import cats.effect.Bracket
import cats.syntax.flatMap._
import cats.syntax.functor._

trait LoggingContext[+F[_]] {
  type Entry = Map[String, String]

  def get: F[Entry]

  def set(a: Entry): F[Unit]

  def getAndSet(a: Entry): F[Entry]

  def update(f: Entry => Entry): F[Unit]

  def withLocal[F1[X] >: F[X], A, E](
      value: Map[String, String]
  )(use: F1[A])(implicit B: Bracket[F1, E]): F1[A] =
    for {
      oldValue <- get.asInstanceOf[F1[Entry]]
      b <- B.bracket(set(oldValue ++ value))(_ => use)(_ => set(oldValue))
    } yield b
}

object LoggingContext {

  def apply[F[_]: LoggingContext]: LoggingContext[F] = implicitly[LoggingContext[F]]
}
