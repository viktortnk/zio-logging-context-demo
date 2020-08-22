package whisk.console.auth

import io.grpc.{Metadata, Status}
import scalapb.zio_grpc.RequestContext
import zio.{IO, Task}

object AuthContext {

  private val AccessToken =
    Metadata.Key.of("access-token", io.grpc.Metadata.ASCII_STRING_MARSHALLER)

  private def extractUser(token: String): User = {
    User("123", "name")
  }

  def extractUser(rc: RequestContext): IO[Status, User] =
    rc.metadata.get(AccessToken).flatMap {
      case Some(token) =>
        Task(extractUser(token))
          .mapError(_ => Status.PERMISSION_DENIED)
      case _ =>
        IO.fail(Status.UNAUTHENTICATED.withDescription("No access!"))
    }
}
