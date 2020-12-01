package nl.hjdskes.aoc

import java.nio.file.{Path, Paths}

import cats.effect.{Blocker, ExitCode, IO, IOApp}
import fs2.{Stream, io, text}

import scala.annotation.unused

object Main extends IOApp {
  def readFile(file: Path): Stream[IO, String] = Stream.resource(Blocker[IO]).flatMap { blocker =>
    io.file
      .readAll[IO](file, blocker, 4096)
      .through(text.utf8Decode)
      .through(text.lines)
      .filter(_.trim.nonEmpty)
  }

  def findSum(l: List[Int], n: Int): Option[List[Int]] = l.combinations(n).find(_.sum == 2020)

  def print(solution: Option[List[Int]]): IO[Unit] = solution match {
    case None => IO(println("No solution found"))
    case Some(solution) => IO(println(s"Solution: ${solution.mkString(" + ")} = 2020, product is ${solution.product}"))
  }

  def run(@unused args: List[String]): IO[ExitCode] =
    for {
      path <- IO(ClassLoader.getSystemResource("input.txt").toURI).map(Paths.get)
      numbers <- readFile(path).map(_.toInt).compile.toList
      _ <- print(findSum(numbers, 2))
      _ <- print(findSum(numbers, 3))
    } yield ExitCode.Success
}
