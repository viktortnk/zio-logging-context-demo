Demo of context-aware logging capabilities in ZIO environment 

```
sbt example/run
```

```
curl -XPOST -H 'access-token: 1234' -H 'x-correlation-id: id22234' -H 'content-type: application/json; charset=utf-8' 'http://127.0.0.1:9090/whisk.console.recipe.v1.RecipeAPI/GetRecipe' -d '{
  "id": "dsfdsf"
}'
```

Info

`com.whisk.util.logging.LoggingContext` - view to underlying context. Only needed when modifying log context or creating a contextual logger. It is ZIO-independent, we can implement similar for Monix

`com.whisk.util.logging.ContextLogger` - wrapper for `log4cats.StructuredLogger`, which uses LoggingContext view

`com.whisk.util.logging.ZIOLoggingContext` - ZIO implementation of LoggingContext, which is using FiberRef

`whisk.console.logging.GrpcLoggingContext` - extracts `correlationId` from grpc request context and puts it into `LoggingContext`


**Usage**

Variant, where we inject StructureLogger from log4cats.
This class doesn't know anything about FiberRef-based logging context, can work as normal, but correlationId will also be printed 
```scala
class RecipeDao[F[_]: Applicative: StructuredLogger] {

  def get(id: String): F[Option[RecipeDetails]] = {
    StructuredLogger[F].info(Map("extraParam" -> "0"))("requesting with id=" + id) *>
      Applicative[F].pure(Some(RecipeDetails(id)))
  }
}
```

Example with 'unsafe' creation of Logger within class
Needs LoggingContext instance to convert make it context-aware
```scala
class RecipeDao2[F[_]: Sync: LoggingContext] {

  private val logger = Slf4jLogger.getLogger[F].withContext

  def get(id: String): F[Option[RecipeDetails]] = {
    logger.info("requesting with id=" + id) *>
      Applicative[F].pure(Some(RecipeDetails(id)))
  }
}
```

Having `LoggingContext` capability allows modification of global or scoped blocks context
```scala
  class Live(recipeDao: RecipeDao[Task])(implicit logger: Logger[Task], lc: LoggingContext[Task]) extends Service {

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
```