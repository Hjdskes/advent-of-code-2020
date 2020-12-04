package nl.hjdskes.aoc

import cats.effect.{ExitCode, IO, IOApp}
import fs2.Pipe

object Main extends IOApp {
  sealed trait Geography
  case object Tree extends Geography
  case object Open extends Geography

  def toGeography(s: String): List[Geography] =
    s.map {
      case '#' => Tree
      case '.' => Open
    }.toList

  def checkSlope(right: Int, down: Int): Pipe[IO, (List[Geography], Long), Int] =
    _.fold(0) {
      case (nrOfTrees, (geographies, lineNr)) =>
        geographies((lineNr.toInt / down) * right % 31) match {
          case Tree if lineNr % down == 0 => nrOfTrees + 1
          case _ => nrOfTrees
        }
    }

  val solve1: Pipe[IO, (List[Geography], Long), Int] =
    checkSlope(3, 1)

  val solve2: Pipe[IO, (List[Geography], Long), Int] =
    _.broadcastThrough(
      checkSlope(1, 1),
      checkSlope(3, 1),
      checkSlope(5, 1),
      checkSlope(7, 1),
      checkSlope(1, 2)
    ).reduce(_ * _)

  def run(args: List[String]): IO[ExitCode] =
    readFile[IO]("days/three/resources/input.txt")
      .map(toGeography)
      .zipWithIndex
      .broadcastThrough(solve1 andThen print("trees"), solve2 andThen print("trees"))
      .compile
      .drain
      .as(ExitCode.Success)
}
