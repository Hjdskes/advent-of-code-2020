package nl.hjdskes.aoc

import cats.Monoid
import cats.effect.IOApp
import cats.effect.{ExitCode, IO}
import cats.implicits._
import fs2.Pipe

object Main extends IOApp {

  def toBinary(s: String): Int =
    s.foldLeft(0) { (n, c) =>
      c match {
        case 'B' | 'R' => 2 * n + 1
        case _ => 2 * n
      }
    }

  def rowColumn(i: Int): (Int, Int) = (i >>> 3, i & 0x07)
  val seatId: (Int, Int) => Int = (row, column) => row * 8 + column
  val max: Monoid[Int] = Monoid.instance(0, (i1, i2) => if (i1 > i2) i1 else i2)

  val solve1: Pipe[IO, Int, Int] = _.foldMonoid(max)

  def solve2(seats: List[Int]): Int = {
    val sum = (seats.min to seats.max).sum
    sum - seats.sum
  }

  def run(args: List[String]): IO[ExitCode] =
    readFile[IO]("days/five/resources/input.txt")
      .map(toBinary)
      .map(rowColumn)
      .map(seatId.tupled)
      .observe(solve1 andThen print("max seat ID"))
      .compile
      .toList
      .map(solve2)
      .flatTap(id => IO(println(s"My seat ID: $id")))
      .as(ExitCode.Success)

}
