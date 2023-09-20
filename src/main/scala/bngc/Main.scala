package bngc

import scala.jdk.CollectionConverters._

object Main {

  def main(args: Array[String]): Unit = {



    def openCampaignForRace(name: String) =
      s"""
<Settings Name="$name" BarracudaAllowed="False" CustomShipsAllowed="False" Video="" FallbackTexture="" />
<Group PointsToUnlock="0">
"""

    val closeGroup = "</Group>"


    def race(level: String, speedClass: SpeedClass = Halberd, difficulty: Difficulty = Expert): String =
      s"""  <Event>
    <Frontend Name="$level Race" Description="" />
    <Awards BronzeValue="0" SilverValue="0" GoldValue="0" PlatinumValue="0" EasyScore="0" HardScore="0" />
    <Mode Gamemode="Race" ModernPhysics="False" FloorHugger="False" SpeedClass="$speedClass" />
    <Modifiers Hardcore="False" Weapons="True" Mirror="False" Hyperspeed="False" Dragspeed="False" ExtraLaps="0" ForcePlayerShip="False" ForceAiShip="False" ForcedPlayerShip="" ForcedAiShip="" ForcedPlayerLivery="0" ForcedPlayerScheme="0" />
    <Ai Count="7" UseSpeedMult="False" SpeedMult="1" Difficulty="$difficulty" />
    <Levels>
      <Level Name="$level" />
    </Levels>
  </Event>
"""

    def openGroupForTournament(pointsToUnlock: Int, speedClass: SpeedClass = Halberd, difficulty: Difficulty = Expert) =
      s"""
<Group PointsToUnlock="$pointsToUnlock">
  <Event>
    <Frontend Name="Tournament" Description="" />
    <Awards BronzeValue="0" SilverValue="0" GoldValue="0" PlatinumValue="0" EasyScore="0" HardScore="0" />
    <Mode Gamemode="Race" ModernPhysics="False" FloorHugger="False" SpeedClass="$speedClass" />
    <Modifiers Hardcore="False" Weapons="True" Mirror="False" Hyperspeed="False" Dragspeed="False" ExtraLaps="0" ForcePlayerShip="False" ForceAiShip="False" ForcedPlayerShip="" ForcedAiShip="" ForcedPlayerLivery="0" ForcedPlayerScheme="0" />
    <Ai Count="0" UseSpeedMult="False" SpeedMult="1" Difficulty="$difficulty" />
    <Levels>"""


    val closeTournament =
      """    </Levels>
  </Event>"""

    import java.nio.charset.StandardCharsets
    import java.nio.file.{Files, Paths}

    val nameOfLevelFile = "standard_levels"
//    val nameOfLevelFile = "enai_siaion_levels"

    val levels = Files.readAllLines(Paths.get(s"src/main/resources/$nameOfLevelFile.txt")).asScala.toList

    val singleRaceEvents = levels.map(l => race(l)).mkString
    val tournamentLevels = levels.map(l => s"""      <Level Name="$l" />""").mkString("\r\n")

    val s =
      s"""
${openCampaignForRace("Ben Test Campaign")}
$singleRaceEvents
$closeGroup
${openGroupForTournament(96)}
$tournamentLevels
$closeTournament
$closeGroup
    """

    val fileName = s"$nameOfLevelFile.xml"
    Files.write(Paths.get(s"out/$fileName"), s.getBytes(StandardCharsets.UTF_8))

    // also write directly to game dir
    Files.write(Paths.get(s"D:/SteamLibrary/steamapps/common/BallisticNG/User/Mods/Campaigns/$fileName"), s.getBytes(StandardCharsets.UTF_8))

  }

}