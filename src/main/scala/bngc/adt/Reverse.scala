package bngc.adt

import bngc.Error._
import cats.data.Validated.{Invalid, Valid}
import cats.data.ValidatedNel

object Reverse {
  sealed trait Reverse
  case object No extends Reverse
  case object Yes extends Reverse
  case object Both extends Reverse
  case object BothSameCampaign extends Reverse

  def validate(
                reverseArg: String
              ): ValidatedNel[Error, Reverse] = reverseArg match {
    case "No" => Valid(No)
    case "Yes" => Valid(Yes)
    case "Both" => Valid(Both)
    case "BothSameCampaign" => Valid(BothSameCampaign)
    case _ => Invalid(InvalidReverse(reverseArg)).toValidatedNel
  }

}
