package whisk.console.server

import com.linecorp.armeria.common.grpc.GrpcSerializationFormats
import com.linecorp.armeria.server.Server
import com.linecorp.armeria.server.docs.{DocService, DocServiceFilter}
import com.linecorp.armeria.server.grpc.GrpcService
import com.linecorp.armeria.server.logging.LoggingService
import io.grpc.ServerServiceDefinition
import io.grpc.reflection.v1alpha.ServerReflectionGrpc
import zio.clock._
import zio.console._
import zio.duration._
import zio._

class ConsoleServer(serviceList: List[ServerServiceDefinition]) {

  def start(): TaskManaged[Server] = {
    val grpcService: GrpcService = serviceList
      .foldLeft(GrpcService.builder()) {
        case (builder, svc) =>
          builder.addService(svc)
      }
      .supportedSerializationFormats(GrpcSerializationFormats.values())
      .jsonMarshallerFactory(_ => ScalaPBJsonMarshaller())
      .enableUnframedRequests(true)
      .build()

    ZManaged.make {
      def server =
        Server
          .builder()
          .http(9090)
          .https(9091)
          .tlsSelfSigned()
          .decorator(LoggingService.newDecorator())
          .service(grpcService)
          .serviceUnder(
            "/docs",
            DocService
              .builder()
              .exclude(DocServiceFilter.ofServiceName(ServerReflectionGrpc.SERVICE_NAME))
              .build()
          )
          .build()
      Task(server) <* ZIO.fromCompletionStage(server.start())
    }(srv => ZIO.fromCompletionStage(srv.closeAsync()).orDie)
  }

}

object ConsoleServer {

  def serverWait: URIO[Console with Clock, ExitCode] = {
    for {
      _ <- putStrLn("Server is running. Press Ctrl-C to stop.")
      _ <- sleep(1.second).forever
    } yield ExitCode.success
  }
}
