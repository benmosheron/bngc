package bngc

import bngc.Error._
import bngc.FileHelper._
import bngc.adt.Difficulty.Difficulty
import bngc.adt.SpeedClass.SpeedClass
import bngc.adt._
import bngc.Placeholder.StringUtil
import bngc.XmlHelper.removeContainerAndFormat
import cats.data.Validated.{Invalid, Valid}
import cats.data.{NonEmptyList, ValidatedNel}
import cats.effect.std.Console
import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.all._
import cats.{Applicative, ApplicativeError}

object Main extends IOApp {

  private final case class ValidatedArgs(
      speedClass: SpeedClass,
      difficulty: Difficulty
  )

  private def reportErrors[F[_]: Console: Applicative](
      errors: NonEmptyList[Error]
  ): F[Unit] =
    errors.map(Console[F].println).sequence_

  private def acceptArgs[F[_]: Console](
      v: ValidatedNel[Error, ValidatedArgs]
  )(implicit ae: ApplicativeError[F, Throwable]): F[ValidatedArgs] = v match {
    case Invalid(errors) =>
      reportErrors(errors) *> ae.raiseError[ValidatedArgs](
        new Exception(errors.toString)
      )
    case Valid(a) => ae.pure(a)
  }

  override def run(args: List[String]): IO[ExitCode] = {

    val pointsToUnlockTournament = 0

    for {
      levelFileNameArg <- Args.readLevelFileArg[IO](args)
      speedClassArg <- Args.readSpeedClassArg[IO](args)
      difficultyArg <- Args.readDifficultyArg[IO](args)
      campaignNameArg <- Args.readCampaignNameArg[IO](args)

      outFileName = s"generated_$levelFileNameArg"
      levelFilePath = s"src/main/resources/levels/$levelFileNameArg.txt"

      a <- validateFileIsReadable[IO](levelFilePath)
      b = SpeedClass.validate(speedClassArg)
      c = Difficulty.validate(difficultyArg)

      speedClassDifficulty = a.product(b).product(c).map {
        case (((), speedClass), difficulty) =>
          ValidatedArgs(speedClass, difficulty)
      }

      acceptedArgs <- acceptArgs[IO](speedClassDifficulty)

      levelNames <- readLines[IO](levelFilePath)
      mainTemplate <- readTemplate[IO]("template")
      singleRaceEventTemplate <- readTemplate[IO]("single_race_event_template")
      tournamentLevelTemplate <- readTemplate[IO]("tournament_level_template")

      singleRaceEvents = levelNames
        .map(levelName =>
          singleRaceEventTemplate
            .withLevelName(levelName)
            .withSpeedClass(acceptedArgs.speedClass)
            .withDifficulty(acceptedArgs.difficulty)
        )
        .mkString("\r\n")

      tournamentLevels = levelNames
        .map(levelName => tournamentLevelTemplate.withLevelName(levelName))
        .mkString("\r\n")

      xmlStringWithContainer = mainTemplate
        .withCampaignName(campaignNameArg)
        .withPointsToUnlockTournament(pointsToUnlockTournament)
        .withSpeedClass(acceptedArgs.speedClass)
        .withDifficulty(acceptedArgs.difficulty)
        .withSingleRaceEvents(singleRaceEvents)
        .withTournamentLevels(tournamentLevels)

      finalString = removeContainerAndFormat(xmlStringWithContainer)

      _ <- writeFile[IO](s"out/$outFileName.xml", finalString)
      _ <- writeFile[IO](
        s"D:/SteamLibrary/steamapps/common/BallisticNG/User/Mods/Campaigns/$outFileName.xml",
        finalString
      )

    } yield ExitCode.Success

  }
}
