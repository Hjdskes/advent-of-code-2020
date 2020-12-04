package nl.hjdskes.aoc

import cats.effect.{ExitCode, IO, IOApp}
import fs2.Pipe
import io.estatico.newtype.macros.newtype

object Main extends IOApp {
  @newtype case class Password(value: String)
  case class Policy(lower: Int, upper: Int, letter: Char)

  def isValidDownTheStreet(policy: Policy, password: Password): Boolean = {
    val occurrences = password.value.filter(_ == policy.letter).length
    policy.lower <= occurrences && occurrences <= policy.upper
  }

  def isValidOTCAS(policy: Policy, password: Password): Boolean = {
    val loc1 = policy.lower - 1
    val loc2 = policy.upper - 1
    password.value.charAt(loc1) == policy.letter ^ password.value.charAt(loc2) == policy.letter
  }

  def parse(string: String): Option[(Policy, Password)] =
    string match {
      case s"$lower-$upper $letter: $password" =>
        Some((Policy(lower.toInt, upper.toInt, letter.head), Password(password)))
      case _ => None
    }

  val partOne: Pipe[IO, (Policy, Password), Boolean] =
    _.map { case (pol, pass) => isValidDownTheStreet(pol, pass) }.filter(identity)
  val partTwo: Pipe[IO, (Policy, Password), Boolean] =
    _.map { case (pol, pass) => isValidOTCAS(pol, pass) }.filter(identity)

  def run(args: List[String]): IO[ExitCode] =
    readFile[IO]("days/two/resources/input.txt")
      .map(parse)
      .unNone
      .broadcastThrough(
        partOne andThen count andThen print("passwords"),
        partTwo andThen count andThen print("passwords")
      )
      .compile
      .drain
      .as(ExitCode.Success)
}
