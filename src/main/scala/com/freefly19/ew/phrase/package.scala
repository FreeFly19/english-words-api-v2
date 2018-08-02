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
    def findAll(page: Long, size: Long): ConnectionIO[List[Phrase]] =
      sql"select id, text, created_at as createdAt from phrases"
        .query[Phrase]
        .stream
        .drop(size * page)
        .take(size)
        .compile
        .to[List]
  }

  class PhraseService(phraseRepository: PhraseRepository,
                      xa: Transactor[IO]) {
    object PageQueryParam extends OptionalQueryParamDecoderMatcher[Long]("page")
    object SizeQueryParam extends OptionalQueryParamDecoderMatcher[Long]("size")

    val service = HttpService[IO] {
      case GET -> Root / "api" / "phrases" :? PageQueryParam(page) :? SizeQueryParam(size) =>
        Ok(phraseRepository.findAll(page.getOrElse(0), size.getOrElse(10)).transact(xa).map(_.asJson))
    }
  }

}
