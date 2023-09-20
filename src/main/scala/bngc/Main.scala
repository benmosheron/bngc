package bngc

import scala.jdk.CollectionConverters._
import scala.xml.XML
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

object Main {

  private val levelFileArgRegex = """--levelFileName (\S+)""".r.unanchored

  private object DefaultArgs {
    val LevelFileName = "standard_levels"
  }

  def main(args: Array[String]): Unit = {

    val argsString = args.mkString(" ")

    val nameOfLevelFile = argsString match {
      case levelFileArgRegex(fileName) => fileName
      case _ => DefaultArgs.LevelFileName
    }

    val speedClass = Halberd
    val difficulty = Expert

    val campaignName = nameOfLevelFile match {
      case "enai_siaion_levels" => s"Enai Siaion $speedClass Campaign"
      case "standard_levels" => s"$speedClass Campaign"
    }

    val outFileName = s"generated_$nameOfLevelFile"

    val pointsToUnlockTournament = 0

    val levelNames = Files
      .readAllLines(
        Paths.get(s"src/main/resources/levels/$nameOfLevelFile.txt")
      )
      .asScala
      .toList


    def readTemplate(name: String): String =
      Files.readString(Paths.get(s"src/main/resources/templates/$name.xml"))

    val mainTemplate = readTemplate("template")
    val singleRaceEventTemplate = readTemplate("single_race_event_template")
    val tournamentLevelTemplate = readTemplate("tournament_level_template")

    import Placeholder.StringUtil

    val singleRaceEvents = levelNames
      .map(levelName =>
        singleRaceEventTemplate
          .withLevelName(levelName)
          .withSpeedClass(speedClass)
          .withDifficulty(difficulty)
      )
      .mkString("\r\n")

    val tournamentLevels = levelNames
      .map(levelName => tournamentLevelTemplate.withLevelName(levelName))
      .mkString("\r\n")

    val xmlWithContainer = mainTemplate
      .withCampaignName(campaignName)
      .withPointsToUnlockTournament(pointsToUnlockTournament)
      .withSpeedClass(speedClass)
      .withDifficulty(difficulty)
      .withSingleRaceEvents(singleRaceEvents)
      .withTournamentLevels(tournamentLevels)

    val container = XML.loadString(xmlWithContainer)

    val settings = (container \ "Settings").head
    val singleRaceGroup =
      (container \ "Group").find(x => x \@ "BngcId" == "SingleRaceGroup").get
    val tournamentGroup =
      (container \ "Group").find(x => x \@ "BngcId" == "TournamentGroup").get

    val pp = new scala.xml.PrettyPrinter(1000, 2)

    val finalString = List(
      settings,
      singleRaceGroup,
      tournamentGroup
    ).map(xml => pp.format(xml))
      .mkString("\r\n")

    Files.write(
      Paths.get(s"out/$outFileName.xml"),
      finalString.getBytes(StandardCharsets.UTF_8)
    )

    // also write directly to game dir
    Files.write(Paths.get(s"D:/SteamLibrary/steamapps/common/BallisticNG/User/Mods/Campaigns/$outFileName.xml"), finalString.getBytes(StandardCharsets.UTF_8))

  }

}
