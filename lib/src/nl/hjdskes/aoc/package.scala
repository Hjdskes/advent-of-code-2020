package nl.hjdskes

import java.nio.file.{Path, Paths}

import cats.effect.{Blocker, ContextShift, Sync}
import cats.implicits._
import fs2.{Pipe, Stream, io, text}

package object aoc {
  private def toPath[F[_]: Sync](path: String): F[Path] =
    Sync[F]
      .delay(ClassLoader.getSystemResource(path))
      .map(_.toURI)
      .map(Paths.get)

  def readFile[F[_]: Sync](file: String)(implicit cs: ContextShift[F]): Stream[F, String] =
    Stream.eval(toPath(file)).flatMap { path =>
      Stream.resource(Blocker[F]).flatMap { blocker =>
        io.file
          .readAll[F](path, blocker, 4096)
          .through(text.utf8Decode)
          .through(text.lines)
          .filter(_.trim.nonEmpty)
      }
    }

  def count[F[_], A]: Pipe[F, A, Int] = _.fold(0)((acc, _) => acc + 1)

  def print[F[_]: Sync, A](what: String): Pipe[F, A, A] =
    _.evalTap(i => Sync[F].delay(println(s"Number of $what: $i")))
}
