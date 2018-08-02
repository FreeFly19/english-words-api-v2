package com.freefly19.ew

import cats.effect._
import com.freefly19.ew.phrase._
import doobie.util.transactor.Transactor
import fs2.StreamApp.ExitCode
import fs2.{Stream, StreamApp}
import io.circe.literal._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext.Implicits.global


object Main extends StreamApp[IO] {
  private val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    sys.env.getOrElse("JDBC_URL", "jdbc:postgresql://localhost/english-words"),
    sys.env.getOrElse("JDBC_USER", "postgres"),
    sys.env.getOrElse("JDBC_PASSWORD", "")
  )

  private val phraseService = new PhraseService(new PhraseRepository, xa)

  private val defaultService = HttpService[IO] {
    case GET -> Root => Ok("English Words API".asJson)
    case _ => NotFound(json"""{"message": "Page not found!"}""")
  }

  override def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode] =
    BlazeBuilder[IO]
      .bindHttp(9090, "0.0.0.0")
      .mountService(defaultService, "/")
      .mountService(phraseService.service, "/")
      .serve
}