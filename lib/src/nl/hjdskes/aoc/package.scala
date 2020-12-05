package nl.hjdskes

import java.nio.file.Paths

import cats.effect.{Blocker, ContextShift, Sync}
import fs2.{Pipe, Stream, io, text}

package object aoc {
  def readFile[F[_]: Sync](file: String)(implicit cs: ContextShift[F]): Stream[F, String] =
    Stream.resource(Blocker[F]).flatMap { blocker =>
      io.file
        .readAll[F](Paths.get(file), blocker, 4096)
        .through(text.utf8Decode)
        .through(text.lines)
    }

  def count[F[_], A]: Pipe[F, A, Int] = _.fold(0)((acc, _) => acc + 1)

  def debug[F[_]: Sync, A]: Pipe[F, A, A] = _.evalTap(s => Sync[F].delay(println(s.toString)))

  def print[F[_]: Sync, A](what: String): Pipe[F, A, A] =
    _.evalTap(i => Sync[F].delay(println(s"Number of $what: $i")))
}
