package bngc

import bngc.Error._
import bngc.FileHelperFs2._
import bngc.PlainData._
import bngc.XmlHelper.removeContainerAndFormat
import bngc.adt.Difficulty.Difficulty
import bngc.adt.Reverse.Reverse
import bngc.adt.SpeedClass.SpeedClass
import bngc.adt._
import bngc.template.{MainTemplate, SingleRaceEventTemplate, TournamentLevelTemplate}
import bngc.typeclasses.Instances._
import cats.data.Validated.{Invalid, Valid}
import cats.data.{NonEmptyList, Validated, ValidatedNel}
import cats.effect.std.Console
import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.all._
import cats.{Applicative, ApplicativeError}
import fs2.io.file.Path

import scala.util.Try

object Main extends IOApp {

  private final case class ValidatedArgs(
      campaignName: CampaignName,
      difficultyNel: NonEmptyList[Difficulty],
      levelFilePath: LevelFilePath,
      outDirPath: OutDirPath,
      pointsToUnlockTournament: Points,
      reverse: Reverse,
      speedClassNel: NonEmptyList[SpeedClass]
  )

  private final case class SingleFileArgs(
      campaignName: CampaignName,
      difficulty: Difficulty,
      levelFilePath: LevelFilePath,
      outDirPath: OutDirPath,
      pointsToUnlockTournament: Points,
      reverse: Reverse,
      speedClass: SpeedClass,
      levelNames: List[LevelName],
      mainTemplate: MainTemplate,
      singleRaceEventTemplate: SingleRaceEventTemplate,
      tournamentLevelTemplate: TournamentLevelTemplate
  ) {
    lazy val modifiedCampaignName = CampaignName(s"${campaignName.s} - $speedClass - $difficulty")
    lazy val levelFileName = levelFilePath.path.fileName.toString.dropRight(levelFilePath.path.extName.length)
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

  private def extend[A](nel: NonEmptyList[A], to: Int): NonEmptyList[A] = {
    val l = nel.length
    if (to <= l) nel
    else nel.appendList(List.fill(to - l)(nel.last))
  }

  private def splitArgs(
      args: ValidatedArgs,
      levelNames: List[LevelName],
      mainTemplate: MainTemplate,
      singleRaceEventTemplate: SingleRaceEventTemplate,
      tournamentLevelTemplate: TournamentLevelTemplate
  ): NonEmptyList[SingleFileArgs] = {

    val longestLength = List(args.speedClassNel.length, args.difficultyNel.length).max

    val speedClassNel = extend(args.speedClassNel, longestLength)
    val difficultyNel = extend(args.difficultyNel, longestLength)

    speedClassNel.zip(difficultyNel).map { case (speedClass, difficulty) =>
      SingleFileArgs(
        args.campaignName,
        difficulty,
        args.levelFilePath,
        args.outDirPath,
        args.pointsToUnlockTournament,
        args.reverse,
        speedClass,
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
          .s
      )
      .mkString("\r\n")

    val tournamentLevels = args.levelNames
      .map(levelName => args.tournamentLevelTemplate.withLevelName(levelName).s)
      .mkString("\r\n")

    val xmlStringWithContainer = args.mainTemplate
      .withCampaignName(args.modifiedCampaignName)
      .withPointsToUnlockTournament(args.pointsToUnlockTournament)
      .withSpeedClass(args.speedClass)
      .withDifficulty(args.difficulty)
      .withSingleRaceEvents(singleRaceEvents)
      .withTournamentLevels(tournamentLevels)

    val finalString = removeContainerAndFormat(xmlStringWithContainer.s)

    val outFileName = getFileName(args)

    for {
      _ <- writeFile[IO](args.outDirPath.path, outFileName, finalString)
      _ <- if (args.outDirPath.path.toString != "out") writeFile[IO](Path("out"), outFileName, finalString) else IO.unit
    } yield ()
  }

  private def getFileName(args: SingleFileArgs): String = {
    val campaignName = args.campaignName.s
      .replace(' ', '_')
      .filter(c => c.isLetterOrDigit || c == '_')
      .toLowerCase
    val lfn = args.levelFileName.toLowerCase
    val speed = args.speedClass.toString.toLowerCase
    val diff = args.difficulty.toString.toLowerCase
    val p = args.pointsToUnlockTournament.i
    s"${campaignName}_${lfn}_${speed}_${diff}_$p"
  }

  override def run(args: List[String]): IO[ExitCode] = {

    for {

      campaignNameArg <- Args.readCampaignNameArg[IO](args)
      difficultyArgs <- Args.readDifficultyArgs[IO](args)
      levelFileNameArg <- Args.readLevelFileArg[IO](args)
      outDirArg <- Args.readOutFileDirectoryArg[IO](args)
      pointsToUnlockTournamentArg <- Args.readPointsToUnlockTournamentArg[IO](args)
      reverseArg <- Args.readReverseArg[IO](args)
      speedClassArgs <- Args.readSpeedClassArgs[IO](args)

      levelFilePathArg = s"src/main/resources/levels/$levelFileNameArg.txt"

      a = Validated.valid[Error, CampaignName](CampaignName(campaignNameArg)).toValidatedNel
      b = Difficulty.validate(difficultyArgs)
      c <- validateFileIsReadable[IO, LevelFilePath](levelFilePathArg)
      d <- validateIsDirectory[IO, OutDirPath](outDirArg)
      e = validatePointsToUnlockTournament(pointsToUnlockTournamentArg).map(Points)
      f = Reverse.validate(reverseArg)
      g = SpeedClass.validate(speedClassArgs)

      acceptedArgs <- acceptArgs[IO]((a, b, c, d, e, f, g).mapN(ValidatedArgs))

      levelNames <- readAllLines[IO, LevelName](acceptedArgs.levelFilePath.path)
      _ <- IO.println(s"Read [${levelNames.length}] level names from file")
      mainTemplate <- readTemplate[IO, MainTemplate]("template")
      singleRaceEventTemplate <- readTemplate[IO, SingleRaceEventTemplate]("single_race_event_template")
      tournamentLevelTemplate <- readTemplate[IO, TournamentLevelTemplate]("tournament_level_template")

      _ <- splitArgs(acceptedArgs, levelNames, mainTemplate, singleRaceEventTemplate, tournamentLevelTemplate)
        .map(createFile)
        .sequence_

    } yield ExitCode.Success

  }
}
