package com.freefly19.ew

import cats.effect.IO
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._


package object phrase {
  case class Phrase(id: Long, text: String)

  class PhraseRepository {
    def findAll: ConnectionIO[List[Phrase]] =
      sql"select id, text from phrases"
        .query[Phrase]
        .to[List]
  }

  class PhraseService(phraseRepository: PhraseRepository,
                      xa: Transactor[IO]) {
    val service = HttpService[IO] {
      case GET -> Root / "api" / "phrases" =>
        Ok(phraseRepository.findAll.transact(xa).map(_.asJson))
    }
  }

}
