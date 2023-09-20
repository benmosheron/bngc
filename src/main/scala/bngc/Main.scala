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

import scala.util.Try

object Main extends IOApp {

  private final case class ValidatedArgs(
      levelFileName: String,
      speedClass: SpeedClass,
      difficulty: Difficulty,
      campaignName: String,
      pointsToUnlockTournament: Int,
      outDir: String
  )

  private def validatePointsToUnlockTournament(
      pointsToUnlockTournamentArg: String
  ): ValidatedNel[Error, Int] =
    Try(pointsToUnlockTournamentArg.toInt).toValidated
      .leftMap(_ => IsNotInteger(pointsToUnlockTournamentArg))
      .toValidatedNel

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

    for {
      levelFileNameArg <- Args.readLevelFileArg[IO](args)
      speedClassArg <- Args.readSpeedClassArg[IO](args)
      difficultyArg <- Args.readDifficultyArg[IO](args)
      campaignNameArg <- Args.readCampaignNameArg[IO](args)
      pointsToUnlockTournamentArg <- Args.readPointsToUnlockTournamentArg[IO](args)
      outDirArg <- Args.readOutFileDirectory[IO](args)

      outFileName = s"generated_$levelFileNameArg"
      levelFilePath = s"src/main/resources/levels/$levelFileNameArg.txt"

      u1 <- validateFileIsReadable[IO](levelFilePath)
      u2 <- validateDirExists[IO](outDirArg)
      a = SpeedClass.validate(speedClassArg)
      b = Difficulty.validate(difficultyArg)
      c = validatePointsToUnlockTournament(pointsToUnlockTournamentArg)

      combinedVal =
        u1.combine(u2).product(a).product(b).product(c).map { case ((((), speedClass), difficulty), points) =>
          ValidatedArgs(levelFileNameArg, speedClass, difficulty, campaignNameArg, points, outDirArg)
        }

      acceptedArgs <- acceptArgs[IO](combinedVal)

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
        .withCampaignName(acceptedArgs.campaignName)
        .withPointsToUnlockTournament(acceptedArgs.pointsToUnlockTournament)
        .withSpeedClass(acceptedArgs.speedClass)
        .withDifficulty(acceptedArgs.difficulty)
        .withSingleRaceEvents(singleRaceEvents)
        .withTournamentLevels(tournamentLevels)

      finalString = removeContainerAndFormat(xmlStringWithContainer)

      _ <- writeFile[IO](s"${acceptedArgs.outDir}/$outFileName.xml", finalString)
      _ <- if (acceptedArgs.outDir != "out") writeFile[IO](s"out/$outFileName.xml", finalString) else IO.unit

    } yield ExitCode.Success

  }
}
