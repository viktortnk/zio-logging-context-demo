package whisk.console.main

import com.whisk.util.logging.{Logging, LoggingContext, MDCLogging, ZIOLoggingContext}
import scalapb.zio_grpc.{RequestContext, ServiceList}
import whisk.console.auth.{AuthContext, User}
import whisk.console.logging.GrpcLoggingContext
import whisk.console.recipe.v1.recipe_api.ZioRecipeApi.RCRecipeAPI
import whisk.console.recipe.v1.{RecipeDao, RecipeDao2, RecipeService}
import whisk.console.server.ConsoleServer
import zio._
import zio.interop.catz.taskConcurrentInstance

object ConsoleApp extends App {

  private def buildServiceList(
      recipeService: RecipeService.Service
  )(implicit lc: LoggingContext[Task]): ServiceList[Any] = {

    ServiceList
      .add[Any, RCRecipeAPI[Any]](
        recipeService
          .transformContextM[User, RequestContext, Any](AuthContext.extractUser)
          .transformContextM[RequestContext, RequestContext, Any](GrpcLoggingContext.transform)
      )
  }

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {

    val serverList: Task[ServiceList[Any]] = for {
      implicit0(lc: LoggingContext[Task]) <- ZIOLoggingContext.make
      implicit0(logger: MDCLogging[Task]) <- Logging.createWithContext[Task]
      recipeDao = new RecipeDao[Task] // this picks up implicit structured logger
      recipeDao2 = new RecipeDao2[Task] // this picks up implicit structured logger
      recipeService =
        new RecipeService.Live(recipeDao, recipeDao2) // pick logger and context, so that it can mutate it
    } yield {
      buildServiceList(recipeService)(lc)
    }

    val sl = ZManaged.fromEffect(serverList).flatMap(_.bindAll)
    val srv = sl.map(new ConsoleServer(_))

    srv.use(_.start().useNow).exitCode *> ConsoleServer.serverWait
  }
}
