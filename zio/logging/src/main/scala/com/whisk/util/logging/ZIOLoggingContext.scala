package com.whisk.util.logging

import zio.{FiberRef, Task, UIO}

object ZIOLoggingContext {

  def make: UIO[LoggingContext[Task]] = {
    for {
      zioRef <- FiberRef.make[Map[String, String]](Map.empty)
    } yield {
      new LoggingContext[Task] {
        override def get: Task[Entry] = zioRef.get

        override def set(a: Entry): Task[Unit] = zioRef.set(a)

        override def getAndSet(a: Entry): Task[Entry] = zioRef.getAndSet(a)

        override def update(f: Entry => Entry): Task[Unit] = zioRef.update(f)
      }
    }
  }
}
