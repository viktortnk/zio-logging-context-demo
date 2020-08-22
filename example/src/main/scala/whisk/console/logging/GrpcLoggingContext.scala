package whisk.console.logging

import com.whisk.util.logging.LoggingContext
import io.grpc.{Metadata, Status}
import scalapb.zio_grpc.RequestContext
import zio.{IO, Task}

object GrpcLoggingContext {

  private val CorrelationId =
    Metadata.Key.of("x-correlation-id", io.grpc.Metadata.ASCII_STRING_MARSHALLER)

  def transform(rc: RequestContext)(implicit LC: LoggingContext[Task]): IO[Status, RequestContext] = {
    for {
      cid <- rc.metadata.get(CorrelationId)
      _ <- LC.set(Map("correlationId" -> cid.getOrElse("default"))).mapError(Status.fromThrowable)
    } yield {
      rc
    }
  }

}
