package com.whisk.util

package object logging {

  type Logger[F[_]] = io.chrisdavenport.log4cats.StructuredLogger[F]
}
