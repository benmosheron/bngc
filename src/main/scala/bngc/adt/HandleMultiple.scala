package bngc.adt

import bngc.Error._
import cats.data.Validated.{Invalid, Valid}
import cats.data.{NonEmptyList, ValidatedNel}

object HandleMultiple {
  sealed trait HandleMultiple
  case object Normal extends HandleMultiple
  case object Explode extends HandleMultiple

  def validate(
      handleMultipleArg: String
  ): ValidatedNel[Error, HandleMultiple] = handleMultipleArg match {
    case "Normal"  => Valid(Normal)
    case "Explode" => Valid(Explode)
    case _         => Invalid(InvalidHandleMultiple(handleMultipleArg)).toValidatedNel
  }

  def validate(difficultyArgs: NonEmptyList[String]): ValidatedNel[Error, NonEmptyList[HandleMultiple]] =
    difficultyArgs.traverse(validate)
}
