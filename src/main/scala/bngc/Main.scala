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
      speedClassNel: NonEmptyList[SpeedClass],
      difficultyNel: NonEmptyList[Difficulty],
      campaignName: String,
      pointsToUnlockTournament: Int,
      outDir: String
  )

  private final case class SingleFileArgs(
      levelFileName: String,
      speedClass: SpeedClass,
      difficulty: Difficulty,
      campaignName: String,
      pointsToUnlockTournament: Int,
      outDir: String,
      levelNames: List[String],
      mainTemplate: String,
      singleRaceEventTemplate: String,
      tournamentLevelTemplate: String
  ) {
    lazy val modifiedCampaignName = s"$campaignName - $speedClass - $difficulty"
  }

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

  def extend[A](nel: NonEmptyList[A], to: Int): NonEmptyList[A] = {
    val l = nel.length
    if (to <= l) nel
    else nel.appendList(List.fill(to - l)(nel.last))
  }

  private def splitArgs(
      args: ValidatedArgs,
      levelNames: List[String],
      mainTemplate: String,
      singleRaceEventTemplate: String,
      tournamentLevelTemplate: String
  ): NonEmptyList[SingleFileArgs] = {

    val longestLength = List(args.speedClassNel.length, args.difficultyNel.length).max

    val speedClassNel = extend(args.speedClassNel, longestLength)
    val difficultyNel = extend(args.difficultyNel, longestLength)

    speedClassNel.zip(difficultyNel).map { case (speedClass, difficulty) =>
      SingleFileArgs(
        args.levelFileName,
        speedClass,
        difficulty,
        args.campaignName,
        args.pointsToUnlockTournament,
        args.outDir,
        levelNames,
        mainTemplate,
        singleRaceEventTemplate,
        tournamentLevelTemplate
      )
    }

  }

  private def createFile(args: SingleFileArgs): IO[Unit] = {
    val singleRaceEvents = args.levelNames
      .map(levelName =>
        args.singleRaceEventTemplate
          .withLevelName(levelName)
          .withSpeedClass(args.speedClass)
          .withDifficulty(args.difficulty)
      )
      .mkString("\r\n")

    val tournamentLevels = args.levelNames
      .map(levelName => args.tournamentLevelTemplate.withLevelName(levelName))
      .mkString("\r\n")

    val xmlStringWithContainer = args.mainTemplate
      .withCampaignName(args.modifiedCampaignName)
      .withPointsToUnlockTournament(args.pointsToUnlockTournament)
      .withSpeedClass(args.speedClass)
      .withDifficulty(args.difficulty)
      .withSingleRaceEvents(singleRaceEvents)
      .withTournamentLevels(tournamentLevels)

    val finalString = removeContainerAndFormat(xmlStringWithContainer)

    val outFileName = getFileName(args)

    for {
      _ <- writeFile[IO](s"${args.outDir}/$outFileName.xml", finalString)
      _ <- if (args.outDir != "out") writeFile[IO](s"out/$outFileName.xml", finalString) else IO.unit
    } yield ()
  }

  private def getFileName(args: SingleFileArgs): String = {
    val campaignName = args.campaignName
      .replace(' ', '_')
      .filter(c => c.isLetterOrDigit || c == '_')
      .toLowerCase
    val lfn = args.levelFileName.toLowerCase
    val speed = args.speedClass.toString.toLowerCase
    val diff = args.difficulty.toString.toLowerCase
    val p = args.pointsToUnlockTournament
    s"${campaignName}_${lfn}_${speed}_${diff}_$p"
  }

  override def run(args: List[String]): IO[ExitCode] = {

    for {
      levelFileNameArg <- Args.readLevelFileArg[IO](args)
      speedClassArgs <- Args.readSpeedClassArgs[IO](args)
      difficultyArgs <- Args.readDifficultyArgs[IO](args)
      campaignNameArg <- Args.readCampaignNameArg[IO](args)
      pointsToUnlockTournamentArg <- Args.readPointsToUnlockTournamentArg[IO](args)
      outDirArg <- Args.readOutFileDirectory[IO](args)

      levelFilePath = s"src/main/resources/levels/$levelFileNameArg.txt"

      u1 <- validateFileIsReadable[IO](levelFilePath)
      u2 <- validateDirExists[IO](outDirArg)
      a = SpeedClass.validate(speedClassArgs)
      b = Difficulty.validate(difficultyArgs)
      c = validatePointsToUnlockTournament(pointsToUnlockTournamentArg)

      combinedVal =
        u1.combine(u2).product(a).product(b).product(c).map { case ((((), speedClassNel), difficultyNel), points) =>
          ValidatedArgs(levelFileNameArg, speedClassNel, difficultyNel, campaignNameArg, points, outDirArg)
        }

      acceptedArgs <- acceptArgs[IO](combinedVal)

      levelNames <- readLines[IO](levelFilePath)
      mainTemplate <- readTemplate[IO]("template")
      singleRaceEventTemplate <- readTemplate[IO]("single_race_event_template")
      tournamentLevelTemplate <- readTemplate[IO]("tournament_level_template")

      _ <- splitArgs(acceptedArgs, levelNames, mainTemplate, singleRaceEventTemplate, tournamentLevelTemplate)
        .map(createFile)
        .sequence_

    } yield ExitCode.Success

  }
}
