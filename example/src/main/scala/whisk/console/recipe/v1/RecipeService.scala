package whisk.console.recipe.v1

import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{Applicative, Monad}
import com.whisk.util.logging.{Logging, MDCLogging}
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

  class Live(recipeDao: RecipeDao[Task], recipeDao2: RecipeDao2[Task])(implicit logging: MDCLogging[Task])
      extends Service {

    override def getRecipe(request: GetRecipeRequest): ZIO[Has[User], Status, GetRecipeResponse] = {

      (for {
        user <- ZIO.service[User]
        logger <- logging.fromName(getClass.getName)
        _ <- logger.info("logging request: " + user)
        _ <- logging.withLocal(Map("localValue" -> "123")) {
          logger.info("local message")
        }
        details <- recipeDao.get(request.id)
        _ <- recipeDao2.get(request.id)
      } yield {
        GetRecipeResponse(details)
      }).mapError(Status.fromThrowable)
    }
  }

}

class RecipeDao[F[_]: Monad: Logging] {

  def get(id: String): F[Option[RecipeDetails]] = {
    for {
      logger <- Logging[F].fromName(getClass.getName)
      _ <- logger.info(Map("extraParam" -> "0"))("requesting with id=" + id)
    } yield {
      Some(RecipeDetails(id))
    }
  }
}

class RecipeDao2[F[_]: Monad: Logging] {

  private val logger = Logging[F].getLoggerFromClass(getClass)

  def get(id: String): F[Option[RecipeDetails]] = {
    logger.info(Map("extraParam" -> "0"))("requesting with id=" + id) *>
      Applicative[F].pure(Some(RecipeDetails(id)))
  }
}
