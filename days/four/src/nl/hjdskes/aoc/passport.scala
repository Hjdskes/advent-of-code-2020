package nl.hjdskes.aoc

import cats.parse.{Numbers, Parser, Parser1}

sealed trait LengthUnit
case object CM extends LengthUnit
case object IN extends LengthUnit

sealed trait EyeColour
case object AMB extends EyeColour
case object BLU extends EyeColour
case object BRN extends EyeColour
case object GRY extends EyeColour
case object GRN extends EyeColour
case object HZL extends EyeColour
case object OTH extends EyeColour

sealed trait Entry
case object CID extends Entry
case class InvalidField(key: String, value: String) extends Entry

sealed trait Field extends Entry
case class BYR(year: Int) extends Field
case class IYR(year: Int) extends Field
case class EYR(year: Int) extends Field
case class HGT(value: Int, unit: LengthUnit) extends Field
case class HCL(color: String) extends Field
case class ECL(color: EyeColour) extends Field
case class PID(id: Long) extends Field

case class Passport(fields: Set[Field], invalid: List[InvalidField])

object parser {

  def checkBounds(num: Int, lower: Int, upper: Int): Parser[Int] =
    if (lower <= num && num <= upper) Parser.pure(num)
    else Parser.failWith(s"Number $num is out of bounds: $lower <= $num <= $upper")

  val whitespace: Parser1[Unit] = Parser.charIn(" \n").void
  val separator: Parser1[Unit] = Parser.char(':')
  val hexChar: Parser1[Unit] = Parser.charIn(('0' to '9') ++ ('a' to 'f') ++ ('A' to 'F')).void
  val hexString: Parser1[String] =
    (Parser.char('#') ~ hexChar ~ hexChar ~ hexChar ~ hexChar ~ hexChar ~ hexChar).string
  def numberBetween(lower: Int, upper: Int): Parser1[Int] =
    Numbers.digits1.flatMap(s => checkBounds(s.toInt, lower, upper))
  val height: Parser1[(Int, LengthUnit)] = (Numbers.digits1 ~ Parser.until1(whitespace)).flatMap {
    case (h, "cm") => checkBounds(h.toInt, 150, 193).map((_, CM))
    case (h, "in") => checkBounds(h.toInt, 59, 76).map((_, IN))
    case (_, unit) => Parser.failWith(s"Invalid length unit: $unit")
  }
  val eyeColour: Parser1[EyeColour] = Parser.until1(whitespace).flatMap {
    case "amb" => Parser.pure(AMB)
    case "blu" => Parser.pure(BLU)
    case "brn" => Parser.pure(BRN)
    case "gry" => Parser.pure(GRY)
    case "grn" => Parser.pure(GRN)
    case "hzl" => Parser.pure(HZL)
    case "oth" => Parser.pure(OTH)
    case otherwise => Parser.failWith[EyeColour](s"Invalid eye color: $otherwise")
  }
  val passportId: Parser1[Long] = // Monad[Parser].replicateA[Char](9, Numbers.digit).string.map(_.toLong)
    (Numbers.digit ~ Numbers.digit ~ Numbers.digit ~ Numbers.digit ~ Numbers.digit ~ Numbers.digit ~ Numbers.digit ~ Numbers.digit ~ Numbers.digit).string
      .map(_.toLong)
  val digitParser: Parser1[Unit] = Numbers.digits1.void

  val validField: Parser1[Field] = (Parser.until1(separator) <* separator).flatMap {
    case "byr" => numberBetween(1920, 2002).map(BYR)
    case "iyr" => numberBetween(2010, 2020).map(IYR)
    case "eyr" => numberBetween(2020, 2030).map(EYR)
    case "hgt" => height.map(HGT.tupled)
    case "hcl" => hexString.map(HCL)
    case "ecl" => eyeColour.map(ECL)
    case "pid" => passportId.map(PID)
    case key => Parser.failWith(s"Invalid field: $key")
  }

  val cID: Parser1[CID.type] = (Parser.until1(separator) <* separator).flatMap {
    case "cid" => digitParser *> Parser.pure(CID)
    case key => Parser.failWith(s"Not a CID: $key")
  }

  val invalidField: Parser1[InvalidField] =
    (Parser.until1(separator) <* separator).flatMap(key => Parser.until1(whitespace).map(InvalidField(key, _)))

  val field: Parser1[Entry] = Parser.oneOf1(validField.backtrack :: cID.backtrack :: invalidField :: Nil)

  val passport: Parser1[Passport] =
    Parser
      .rep1Sep(field, 1, whitespace)
      .map(_.toList.filter(_ != CID))
      .map(fields =>
        Passport(fields.collect { case f: Field => f }.toSet, fields.collect { case f: InvalidField => f })
      )
}
