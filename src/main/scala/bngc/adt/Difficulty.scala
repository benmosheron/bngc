package bngc.adt

import bngc.Error._
import cats.data.Validated.{Invalid, Valid}
import cats.data.{NonEmptyList, ValidatedNel}

object Difficulty {
  sealed trait Difficulty
  case object Novice extends Difficulty
  case object Experienced extends Difficulty
  case object Expert extends Difficulty
  case object Elite extends Difficulty
  case object Hardcore extends Difficulty

  def validate(
      difficultyArg: String
  ): ValidatedNel[Error, Difficulty] = difficultyArg match {
    case "Novice"      => Valid(Novice)
    case "Experienced" => Valid(Experienced)
    case "Expert"      => Valid(Expert)
    case "Elite"       => Valid(Elite)
    case "Hardcore"    => Valid(Hardcore)
    case _             => Invalid(InvalidDifficulty(difficultyArg)).toValidatedNel
  }

  def validate(difficultyArgs: NonEmptyList[String]): ValidatedNel[Error, NonEmptyList[Difficulty]] =
    difficultyArgs.traverse(validate)
}
