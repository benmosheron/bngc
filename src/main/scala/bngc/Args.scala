package bngc

import cats.Applicative
import cats.effect.std.Console
import cats.syntax.all._

object Args {

  private val levelFileNameParam = "--levelFileName"
  private val levelFileDefault = "standard_levels"

  private val speedClassParam = "--speedClass"
  private val speedClassDefault = "Halberd"

  private val difficultyParam = "--difficulty"
  private val difficultyDefault = "Expert"

  private val campaignNameParam = "--campaignName"
  private val campaignNameDefault = "Default Campaign Name"

  def readLevelFileArg[F[_]: Console: Applicative](
      args: List[String]
  ): F[String] = readArg(args)(levelFileNameParam, levelFileDefault)

  def readSpeedClassArg[F[_]: Console: Applicative](
      args: List[String]
  ): F[String] = readArg(args)(speedClassParam, speedClassDefault)

  def readDifficultyArg[F[_]: Console: Applicative](
      args: List[String]
  ): F[String] = readArg(args)(difficultyParam, difficultyDefault)

  def readCampaignNameArg[F[_]: Console: Applicative](
      args: List[String]
  ): F[String] =
    readArg(args)(campaignNameParam, campaignNameDefault)

  private def readArg[F[_]: Console: Applicative](
      args: List[String]
  )(param: String, default: String): F[String] = {
    args.sliding(2).collectFirst { case `param` :: arg :: Nil =>
      arg
    } match {
      case None =>
        Console[F]
          .println(s"No $param in args, using default [$default]")
          .as(default)
      case Some(arg) =>
        Console[F]
          .println(s"Got $param from args [$arg]")
          .as(arg)
    }
  }

}
