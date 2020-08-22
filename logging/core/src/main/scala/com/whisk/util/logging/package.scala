package com.whisk.util

import cats.FlatMap

package object logging {

  type Logger[F[_]] = io.chrisdavenport.log4cats.StructuredLogger[F]

  object Logger {

    def flatten[F[_]: FlatMap](underlying: F[Logger[F]]): Logger[F] =
      new EmbedLogger[F](underlying)
  }
}
