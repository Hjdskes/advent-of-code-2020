package nl.hjdskes.aoc

import java.nio.file.{Path, Paths}

import cats.effect.{Blocker, ExitCode, IO, IOApp}
import fs2.{Stream, io, text}

import scala.annotation.{tailrec, unused}

object Main extends IOApp {
  def readFile(file: Path): Stream[IO, String] = Stream.resource(Blocker[IO]).flatMap { blocker =>
    io.file
      .readAll[IO](file, blocker, 4096)
      .through(text.utf8Decode)
      .through(text.lines)
      .filter(_.trim.nonEmpty)
  }

  @tailrec
  def problemOneEfficiently(l: List[Int]): Option[Int] = l match {
    case Nil => None
    case h :: t =>
      val x = 2020 - h
      if (t.contains(x)) Some(h * x) else problemOneEfficiently(t)
  }

  def findSum(l: List[Int], n: Int): Option[List[Int]] = l.combinations(n).find(_.sum == 2020)

  def print1(solution: Option[Int]): IO[Unit] = solution match {
    case None => IO(println("No solution found"))
    case Some(solution) => IO(println(s"Solution: $solution"))
  }

  def print2(solution: Option[List[Int]]): IO[Unit] = solution match {
    case None => IO(println("No solution found"))
    case Some(solution) => IO(println(s"Solution: ${solution.mkString(" + ")} = 2020, product is ${solution.product}"))
  }

  def run(@unused args: List[String]): IO[ExitCode] =
    for {
      path <- IO(ClassLoader.getSystemResource("input.txt").toURI).map(Paths.get)
      numbers <- readFile(path).map(_.toInt).compile.toList
      _ <- print1(problemOneEfficiently(numbers))
      _ <- print2(findSum(numbers, 2))
      _ <- print2(findSum(numbers, 3))
    } yield ExitCode.Success
}
