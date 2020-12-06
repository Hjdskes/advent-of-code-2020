package nl.hjdskes.aoc

import cats.effect.IOApp
import cats.effect.{ExitCode, IO}
import cats.implicits._
import fs2.{Chunk, Pipe}

object Main extends IOApp {

  val solve1: Pipe[IO, Chunk[String], Int] = _.map(_.toList.mkString).map(_.toSet).map(_.size).foldMonoid
  val solve2: Pipe[IO, Chunk[String], Int] = _.map { chunk =>
    val groupSize = chunk.size
    val groupAnswers = chunk.toList.mkString
    val charCount = groupAnswers.foldLeft(Map.empty[Char, Int])((m, c) => m.updatedWith(c)(_.map(_ + 1).orElse(1.some)))
    charCount.filter(kv => kv._2 == groupSize).size
  }.foldMonoid

  def run(args: List[String]): IO[ExitCode] =
    readFile[IO]("days/six/resources/input.txt")
      .split(_.isEmpty)
      .broadcastThrough(solve1 andThen print("yesses"), solve2 andThen print("common yesses"))
      .compile
      .drain
      .as(ExitCode.Success)

}
