package com.freefly19.ew

import java.sql.Timestamp

import cats.effect.IO
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._


package object phrase {
  implicit val timestampEncoder: Encoder[Timestamp] = a => a.getTime.asJson

  case class Phrase(id: Long, text: String, createdAt: Timestamp)

  class PhraseRepository {
    def findAll: ConnectionIO[List[Phrase]] =
      sql"select id, text, created_at as createdAt from phrases"
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
