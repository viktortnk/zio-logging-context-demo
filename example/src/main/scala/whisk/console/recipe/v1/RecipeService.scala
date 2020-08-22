package whisk.console.recipe.v1

import cats.Applicative
import cats.effect.Sync
import cats.syntax.apply._
import com.whisk.util.logging.LoggingContext
import com.whisk.util.logging.syntax._
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.chrisdavenport.log4cats.{Logger, StructuredLogger}
import io.grpc.Status
import whisk.console.auth.User
import whisk.console.recipe.v1.recipe.RecipeDetails
import whisk.console.recipe.v1.recipe_api.ZioRecipeApi.ZRecipeAPI
import whisk.console.recipe.v1.recipe_api.{GetRecipeRequest, GetRecipeResponse}
import zio.interop.catz.taskConcurrentInstance
import zio.{Has, Task, ZIO}

object RecipeService {

  type Service = ZRecipeAPI[Any, Has[User]]

  type RecipeService = Has[Service]

  class Live(recipeDao: RecipeDao[Task])(implicit logger: Logger[Task], lc: LoggingContext[Task])
      extends Service {

    override def getRecipe(request: GetRecipeRequest): ZIO[Has[User], Status, GetRecipeResponse] = {

      (for {
        user <- ZIO.service[User]
        _ <- logger.info("logging request: " + user)
        _ <- lc.withLocal(Map("localValue" -> "123")) {
          logger.info("local message")
        }
        details <- recipeDao.get(request.id)
      } yield {
        GetRecipeResponse(details)
      }).mapError(Status.fromThrowable)
    }
  }

}

class RecipeDao[F[_]: Applicative: StructuredLogger] {

  def get(id: String): F[Option[RecipeDetails]] = {
    StructuredLogger[F].info(Map("extraParam" -> "0"))("requesting with id=" + id) *>
      Applicative[F].pure(Some(RecipeDetails(id)))
  }
}

class RecipeDao2[F[_]: Sync: LoggingContext] {

  private val logger = Slf4jLogger.getLogger[F].withContext

  def get(id: String): F[Option[RecipeDetails]] = {
    logger.info(Map("extraParam" -> "0"))("requesting with id=" + id) *>
      Applicative[F].pure(Some(RecipeDetails(id)))
  }
}
