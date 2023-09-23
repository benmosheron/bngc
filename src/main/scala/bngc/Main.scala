package bngc

import bngc.Error._
import bngc.FileHelperFs2._
import bngc.PlainData._
import bngc.XmlHelper.removeContainerAndFormat
import bngc.adt.Difficulty.Difficulty
import bngc.adt.HandleMultiple.HandleMultiple
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
      handleMultiple: HandleMultiple,
      levelFilePathNel: NonEmptyList[LevelFilePath],
      outDirPath: OutDirPath,
      pointsToUnlockTournament: Points,
      speedClassNel: NonEmptyList[SpeedClass]
  )

  private final case class SingleFileArgs(
      campaignName: CampaignName,
      difficulty: Difficulty,
      levelFilePath: LevelFilePath,
      outDirPath: OutDirPath,
      pointsToUnlockTournament: Points,
      speedClass: SpeedClass,
      levelNames: List[LevelName],
      mainTemplate: MainTemplate,
      singleRaceEventTemplate: SingleRaceEventTemplate,
      tournamentLevelTemplate: TournamentLevelTemplate
  ) {
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
      levelPathAndNames: NonEmptyList[LevelsAndFilePath],
      mainTemplate: MainTemplate,
      singleRaceEventTemplate: SingleRaceEventTemplate,
      tournamentLevelTemplate: TournamentLevelTemplate
  ): NonEmptyList[SingleFileArgs] = {

    val split: NonEmptyList[(SpeedClass, Difficulty, LevelsAndFilePath)] = args.handleMultiple match {
      case HandleMultiple.Normal =>
        val longestLength = List(args.speedClassNel.length, args.difficultyNel.length).max
        val speedClassNel = extend(args.speedClassNel, longestLength)
        val difficultyNel = extend(args.difficultyNel, longestLength)
        val levelInfoNel = extend(levelPathAndNames, longestLength)
        speedClassNel.zip(difficultyNel).zip(levelInfoNel).map { case ((a, b), c) => (a, b, c) }
      case HandleMultiple.Explode =>
        for {
          speedClass <- args.speedClassNel
          difficulty <- args.difficultyNel
          levelInfo <- levelPathAndNames
        } yield (speedClass, difficulty, levelInfo)
    }

    split.map { case (speedClass, difficulty, levelInfo) =>
      SingleFileArgs(
        args.campaignName,
        difficulty,
        levelInfo.path,
        args.outDirPath,
        args.pointsToUnlockTournament,
        speedClass,
        levelInfo.names,
        mainTemplate,
        singleRaceEventTemplate,
        tournamentLevelTemplate
      )
    }

  }

  def generateCampaignName(args: SingleFileArgs): CampaignName = {

    def name(niceName: String): String = s"$niceName - ${args.speedClass} - ${args.difficulty}"

    CampaignName(args.levelFileName match {
      // Because I chose ugly level file names...
      case "standard_levels"                        => name("Standard")
      case "standard_levels_reverse"                => name("Standard Reverse")
      case "standard_levels_forward_and_reverse"    => name("Standard All")
      case "enai_siaion_levels"                     => name("Enai Siaion")
      case "enai_siaion_levels_reverse"             => name("Enai Siaion Reverse")
      case "enai_siaion_levels_forward_and_reverse" => name("Enai Siaion All")
      case "bro_bama_levels"                        => name("Bro Bama")
      case "bro_bama_levels_reverse"                => name("Bro Bama Reverse")
      case "bro_bama_levels_forward_and_reverse"    => name("Bro Bama All")
    })
  }

  private def createFile(args: SingleFileArgs): IO[Unit] = {
    val finalCampaignName = args.campaignName match {
      case CampaignName("$generate") => generateCampaignName(args)
      case _                         => CampaignName(s"${args.campaignName.s} - ${args.speedClass} - ${args.difficulty}")
    }

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
      .withCampaignName(finalCampaignName)
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
    val campaignName = args.levelFileName
      .replace(' ', '_')
      .filter(c => c.isLetterOrDigit || c == '_')
      .toLowerCase
    val lfn = args.levelFileName.toLowerCase
    val speed = args.speedClass.toString.toLowerCase
    val diff = args.difficulty.toString.toLowerCase
    val p = args.pointsToUnlockTournament.i
    s"${campaignName}_${lfn}_${speed}_${diff}_$p"
  }

  private def validateAllFilesReadable(
      levelFilePathArgs: NonEmptyList[String]
  ): IO[ValidatedNel[Error, NonEmptyList[LevelFilePath]]] =
    levelFilePathArgs.traverse(validateFileIsReadable[IO, LevelFilePath]).map(_.sequence)

  private def readAllLevelFiles(
      levelFiles: NonEmptyList[LevelFilePath]
  ): IO[NonEmptyList[LevelsAndFilePath]] = levelFiles
    .map(_.path)
    .traverse(readAllLines[IO, LevelName])
    .map(_.zip(levelFiles).map(LevelsAndFilePath.tupled))

  override def run(args: List[String]): IO[ExitCode] = {

    for {

      campaignNameArg <- Args.readCampaignNameArg[IO](args)
      difficultyArgs <- Args.readDifficultyArgs[IO](args)
      handleMultipleArg <- Args.readHandleMultipleArg[IO](args)
      levelFileNameArgs <- Args.readLevelFileArgs[IO](args)
      outDirArg <- Args.readOutFileDirectoryArg[IO](args)
      pointsToUnlockTournamentArg <- Args.readPointsToUnlockTournamentArg[IO](args)
      speedClassArgs <- Args.readSpeedClassArgs[IO](args)

      a = Validated.valid[Error, CampaignName](CampaignName(campaignNameArg)).toValidatedNel
      b = Difficulty.validate(difficultyArgs)
      c = HandleMultiple.validate(handleMultipleArg)
      d <- validateAllFilesReadable(levelFileNameArgs.map(arg => s"src/main/resources/levels/$arg.txt"))
      e <- validateIsDirectory[IO, OutDirPath](outDirArg)
      f = validatePointsToUnlockTournament(pointsToUnlockTournamentArg).map(Points)
      g = SpeedClass.validate(speedClassArgs)

      acceptedArgs <- acceptArgs[IO]((a, b, c, d, e, f, g).mapN(ValidatedArgs))

      levelNames <- readAllLevelFiles(acceptedArgs.levelFilePathNel)
      mainTemplate <- readTemplate[IO, MainTemplate]("template")
      singleRaceEventTemplate <- readTemplate[IO, SingleRaceEventTemplate]("single_race_event_template")
      tournamentLevelTemplate <- readTemplate[IO, TournamentLevelTemplate]("tournament_level_template")

      _ <- splitArgs(acceptedArgs, levelNames, mainTemplate, singleRaceEventTemplate, tournamentLevelTemplate)
        .map(createFile)
        .sequence_

    } yield ExitCode.Success

  }
}
