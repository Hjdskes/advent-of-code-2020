package nl.hjdskes.aoc

import cats.effect.{ExitCode, IO, IOApp}

import scala.annotation.{tailrec, unused}

object Main extends IOApp {
  def findSum(l: List[Int], n: Int): Option[List[Int]] = l.combinations(n).find(_.sum == 2020)

  @tailrec
  def problemOneEfficiently(l: List[Int]): Option[Int] = l match {
    case Nil => None
    case h :: t =>
      val x = 2020 - h
      if (t.contains(x)) Some(h * x) else problemOneEfficiently(t)
  }

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
      numbers <- readFile[IO]("days/one/resources/input.txt").map(_.toInt).compile.toList
      _ <- print1(problemOneEfficiently(numbers))
      _ <- print2(findSum(numbers, 2))
      _ <- print2(findSum(numbers, 3))
    } yield ExitCode.Success
}
