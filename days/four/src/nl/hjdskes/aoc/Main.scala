package nl.hjdskes.aoc

import cats.effect.{ExitCode, IO, IOApp}
import fs2.Pipe
import nl.hjdskes.aoc.parser.passport

object Main extends IOApp {

//   val solve1: Pipe[IO, String, Boolean] =
//     _.map { s =>
//       s.count(_ == ':') match {
//         case 8 => true
//         case 7 if !s.contains("cid") => true
//         case _ => false
//       }
//     }.filter(identity)

  val solve1: Pipe[IO, Passport, Boolean] =
    _.map(passport => (passport.fields.size + passport.invalid.size) == 7).filter(identity)

  val solve2: Pipe[IO, Passport, Boolean] =
    _.map(_.fields.size == 7).filter(identity)

  def run(args: List[String]): IO[ExitCode] =
    readFile[IO]("days/four/resources/input.txt")
    // TODO: Can we make this more efficient?
      .split(_.isEmpty)
      .map(_.toList.mkString(" "))
      .through(debug)
      .map(passport.parseAll)
      .evalTap {
        case Left(e) => IO(println(s"Parsing error: $e"))
        case _ => IO.pure(())
      }
      .collect { case Right(passport) => passport }
      .broadcastThrough(
        solve1 andThen count andThen print("passports"),
        solve2 andThen count andThen print("passports")
      )
      .compile
      .drain
      .as(ExitCode.Success)
}
