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

`com.whisk.util.logging.LoggingContext` - view to underlying context. Likely only need to be used directly when constructing program on top level. It is ZIO-independent, we can implement similar for Monix

`com.whisk.util.logging.Logging` - Type for constructing Loggers and accessing/modifying underlying context

`com.whisk.util.logging.ZIOLoggingContext` - ZIO implementation of LoggingContext, which is using FiberRef

`whisk.console.logging.GrpcLoggingContext` - extracts `correlationId` from grpc request context and puts it into `LoggingContext`


**Usage**

Example of effectful creation of Logger. The returned type is `log4cats.StructuredLogger`
```scala
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

```

Example with 'unsafe' creation of Logger within class
```scala
class RecipeDao2[F[_]: Monad: Logging] {

  private val logger = Logging[F].getLoggerFromClass(getClass)

  def get(id: String): F[Option[RecipeDetails]] = {
    logger.info(Map("extraParam" -> "0"))("requesting with id=" + id) *>
      Applicative[F].pure(Some(RecipeDetails(id)))
  }
}
```

Injecting more powerful MDCLogging[F[_]] construct when needed the access to underlying context
```scala
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
```