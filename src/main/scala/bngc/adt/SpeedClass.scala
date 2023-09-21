package bngc.adt

import bngc.Error._
import cats.data.Validated.{Invalid, Valid}
import cats.data.{NonEmptyList, ValidatedNel}

object SpeedClass {
  sealed trait SpeedClass
  case object Toxic extends SpeedClass
  case object Apex extends SpeedClass
  case object Halberd extends SpeedClass
  case object Spectre extends SpeedClass
  case object Zen extends SpeedClass

  private def validate(
      speedClassArg: String
  ): ValidatedNel[Error, SpeedClass] = speedClassArg match {
    case "Toxic"   => Valid(Toxic)
    case "Apex"    => Valid(Apex)
    case "Halberd" => Valid(Halberd)
    case "Spectre" => Valid(Spectre)
    case "Zen"     => Valid(Zen)
    case _         => Invalid(InvalidSpeedClass(speedClassArg)).toValidatedNel
  }

  def validate(speedClassArgs: NonEmptyList[String]): ValidatedNel[Error, NonEmptyList[SpeedClass]] =
    speedClassArgs.traverse(validate)

}
