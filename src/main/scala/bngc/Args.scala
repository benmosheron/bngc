package bngc

import cats.Applicative
import cats.data.NonEmptyList
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
  private val campaignNameDefault = "Custom Campaign"

  private val pointsToUnlockTournamentParam = "--pointsToUnlockTournament"
  private val pointsToUnlockTournamentDefault = 0

  private val outFileDirectoryParam = "--outFileDirectory"
  private val outFileDirectoryDefault = "out"

  private val reverseParam = "--reverse"
  private val reverseDefault = "No"

  private val allParams = Set(
    levelFileNameParam,
    speedClassParam,
    difficultyParam,
    campaignNameParam,
    pointsToUnlockTournamentParam,
    outFileDirectoryParam,
    reverseParam
  )

  def readLevelFileArg[F[_]: Console: Applicative](
      args: List[String]
  ): F[String] = readArg(args)(levelFileNameParam, levelFileDefault)

  def readSpeedClassArgs[F[_]: Console: Applicative](
      args: List[String]
  ): F[NonEmptyList[String]] = readArgNel(args)(speedClassParam, speedClassDefault)

  def readDifficultyArgs[F[_]: Console: Applicative](
      args: List[String]
  ): F[NonEmptyList[String]] = readArgNel(args)(difficultyParam, difficultyDefault)

  def readCampaignNameArg[F[_]: Console: Applicative](
      args: List[String]
  ): F[String] = readArg(args)(campaignNameParam, campaignNameDefault)

  def readPointsToUnlockTournamentArg[F[_]: Console: Applicative](
      args: List[String]
  ): F[String] = readArg(args)(pointsToUnlockTournamentParam, pointsToUnlockTournamentDefault.toString)

  def readOutFileDirectoryArg[F[_]: Console: Applicative](
      args: List[String]
  ): F[String] = readArg(args)(outFileDirectoryParam, outFileDirectoryDefault)

  def readReverseArg[F[_]: Console: Applicative](
      args: List[String]
  ): F[String] = readArg(args)(reverseParam, reverseDefault)

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

  private def readArgNel[F[_]: Console: Applicative](
      args: List[String]
  )(param: String, default: String): F[NonEmptyList[String]] = {

    val argList = args.indexOf(param) match {
      case -1 => List()
      case i =>
        val rem = args.drop(i + 1)
        val argsUntilNextParam = rem.indexWhere(s => allParams.contains(s)) match {
          case -1 => rem
          case j  => rem.dropRight(rem.length - j)
        }
        argsUntilNextParam match {
          case Nil => List()
          case l   => l
        }
    }

    argList match {
      case Nil =>
        Console[F]
          .println(s"No $param in args, using default [$default]")
          .as(NonEmptyList.one(default))
      case one :: Nil =>
        Console[F]
          .println(s"Got $param from args [$one]")
          .as(NonEmptyList.one(one))
      case l @ head :: tail =>
        Console[F]
          .println(s"Got [${l.length}] $param params: [${l.mkString(", ")}]")
          .as(NonEmptyList(head, tail))

    }
  }

}
