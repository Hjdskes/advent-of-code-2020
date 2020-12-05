package nl.hjdskes.aoc

import cats.Eq
import cats.parse.Parser
import nl.hjdskes.aoc.parser._
import org.scalacheck.Prop.forAll
import org.scalacheck.Gen
import org.scalacheck.Gen.{hexChar => hexCharGen}

object generators {
  val whitespaceGen: Gen[Char] = Gen.frequency((7, Gen.oneOf(' ', '\n')), (3, Gen.alphaChar))
  val separatorGen: Gen[Char] = Gen.frequency((5, Gen.const(':')), (5, Gen.alphaChar))
  val hexStringGen: Gen[String] = Gen.stringOfN(6, hexCharGen).map('#' +: _)
  val lengthUnitGen: Gen[LengthUnit] = Gen.oneOf(CM, IN)
  val heightGen: Gen[(Int, LengthUnit)] = lengthUnitGen.flatMap(u => Gen.posNum[Int].map((_, u)))
  val eyeColourGen: Gen[EyeColour] = Gen.oneOf(AMB, BLU, BRN, GRY, GRN, HZL, OTH)
  val passportIdGen: Gen[String] = Gen.stringOfN(9, Gen.numChar)
  val fieldGen: Gen[String] = Gen.oneOf(
    Gen.choose(1920, 2002).map(_.toString).map("byr:" ++ _),
    Gen.choose(2010, 2020).map(_.toString).map("iyr:" ++ _),
    Gen.choose(2020, 2030).map(_.toString).map("eyr:" ++ _),
    Gen
      .oneOf("cm", "in")
      .flatMap {
        case "cm" => Gen.choose(150, 193).map(_.toString ++ "cm")
        case "in" => Gen.choose(59, 76).map(_.toString ++ "in")
      }
      .map("hgt:" ++ _),
    hexStringGen.map("hcl:" ++ _),
    eyeColourGen.map(_.toString.toLowerCase).map("ecl:" ++ _),
    passportIdGen.map("pid:" ++ _)
  )
}

class ParserTest extends munit.ScalaCheckSuite {
  import generators._

  def parseTest[A: Eq](p: Parser[A], str: String, a: A): Unit =
    p.parse(str) match {
      case Right((_, res)) =>
        assert(Eq[A].eqv(a, res), s"expected: $a, but got $res")
      case Left(errs) =>
        assert(false, errs.toString)
    }

  property("whitespace parses [ \n]") {
    forAll(whitespaceGen) { (c: Char) =>
      c match {
        case ' ' | '\n' => parseTest(whitespace, c.toString, ())
        case _ => assert(whitespace.parse(c.toString).isLeft, s"Expected parse failure when parsing $c")
      }
    }
  }

  test("separator parses :") {
    forAll(separatorGen) { (c: Char) =>
      c match {
        case ':' => parseTest(separator, c.toString, ())
        case _ => assert(separator.parse(c.toString).isLeft, s"Expected parse failure when parsing $c")
      }
    }
  }

  // TODO: test failure cases?
  property("hexChar parses [0-9a-fA-F]") {
    forAll(hexCharGen)((hex: Char) => parseTest(hexChar, hex.toString, ()))
  }

  // TODO: test failure cases?
  property("hexString parses #[0-9a-fA-F]{6}") {
    forAll(hexStringGen)((hex: String) => parseTest(hexString, hex, hex))
  }

  // TODO: test failure cases?
  property("numberBetween parses [0-9]+ within bounds") {
    forAll(Gen.chooseNum(10, 100))((n: Int) => parseTest(numberBetween(10, 100), n.toString, n))
  }

  implicit val lengthUnitEq: Eq[LengthUnit] = Eq.fromUniversalEquals

  property("height parses 150-193cm or 59-76in") {
    forAll(heightGen) {
      case h @ (length, CM) if 150 <= length && length <= 193 => parseTest(height, length.toString ++ "cm", h)
      case (length, CM) =>
        assert(height.parse(length.toString ++ "cm").isLeft, s"When parsing CM, 150 <= length <= 193 but was $length")
      case h @ (length, IN) if 59 <= length && length <= 76 => parseTest(height, length.toString ++ "in", h)
      case (length, IN) =>
        assert(height.parse(length.toString ++ "in").isLeft, s"When parsing IN, 59 <= length <= 76 but was $length")
    }
  }

  implicit val eyeColourEq: Eq[EyeColour] = Eq.fromUniversalEquals

  // TODO: test failure cases?
  property("eyeColour parses [amb|blu|brn|gry|grn|hzl|oth]") {
    forAll(eyeColourGen)((color: EyeColour) => parseTest(eyeColour, color.toString.toLowerCase, color))
  }

  // TODO: test failure cases?
  property("passportId parses [0-9]{9}") {
    forAll(passportIdGen)((id: String) => parseTest(passportId, id, id.toLong))
  }

  // TODO: test failure cases?
  property("digitParser parses [0-9]+") {
    forAll(Gen.numStr.suchThat(_.nonEmpty))((s: String) => parseTest(digitParser, s, ()))
  }

  // TODO: test failure cases?
  property("validField should parse any valid field") {
    forAll(fieldGen)((field: String) => parseTest(validField.string, field, field))
  }

  implicit val cIdEq: Eq[CID.type] = Eq.fromUniversalEquals

  // TODO: test failure cases?
  property("cID should eat any number") {
    forAll(Gen.numStr.suchThat(_.nonEmpty))((s: String) => parseTest(cID, "cid:" ++ s, CID))
  }

  implicit val entryEq: Eq[Entry] = Eq.instance {
    case (InvalidField(k1, v1), InvalidField(k2, v2)) => k1 == k2 && v1 == v2
    case _ => false
  }

  test("field should succeed") {
    parseTest(field, "ecl:zzz", InvalidField("ecl", "zzz"))
  }

  implicit val passportEq: Eq[Passport] = Eq.fromUniversalEquals

  test("iyr:2021 eyr:2033 ecl:gmt hgt:59cm byr:1967 pid:2498700612") {
    parseTest[Passport](
      passport,
      "iyr:2021 eyr:2033 ecl:gmt hgt:59cm byr:1967 pid:2498700612",
      Passport(
        Set(BYR(1967), PID(249870061L)),
        List(
          InvalidField("iyr", "2021"),
          InvalidField("eyr", "2033"),
          InvalidField("ecl", "gmt"),
          InvalidField("hgt", "59cm")
        )
      )
    )
  }
}
