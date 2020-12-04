package nl.hjdskes

import java.nio.file.{Path, Paths}

import cats.effect.{Blocker, ContextShift, Sync}
import cats.implicits._
import fs2.{Stream, io, text}

package object aoc {
  private def toPath[F[_]: Sync](path: String): F[Path] =
    Sync[F]
      .delay(ClassLoader.getSystemResource(path))
      .map(_.toURI)
      .map(Paths.get)

  def readFile[F[_]: Sync](file: String)(implicit cs: ContextShift[F]): Stream[F, String] = {
    Stream.eval(toPath(file)).flatMap { path =>
      Stream.resource(Blocker[F]).flatMap { blocker =>
        io.file
          .readAll[F](path, blocker, 4096)
          .through(text.utf8Decode)
          .through(text.lines)
          .filter(_.trim.nonEmpty)
      }
    }
  }
}
