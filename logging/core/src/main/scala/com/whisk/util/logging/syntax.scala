package com.whisk.util.logging

import cats.FlatMap
import io.chrisdavenport.log4cats.StructuredLogger

object syntax {

  implicit class StructuredLoggerOps[F[_]](logger: StructuredLogger[F]) {

    def withContext(implicit LC: LoggingContext[F], flatMap: FlatMap[F]): StructuredLogger[F] = {
      ContextLogger.wrap(logger, LC)
    }
  }
}
