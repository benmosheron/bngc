object Main {

  def main(args: Array[String]): Unit = {

    val standardLevels = List(
      "Cassandra",
      "Zephyr Ridge",
      "Surge",
      "Harpstone",
      "Aciknovae",
      "Zephyr Climb",
      "Nova Split",
      "Luna",
      "Dover",
      "Ishtar Citadel",
      "Arrivon XI",
      "Omega Harbour",
      "Utah Project",
      "Marina Rush",
      "Atlantica",
      "Maceno Bay",
      "Therma Fumos",
      "Maceno Interchange",
      "Freyr Falls",
      "Alto Overseer",
      "Maceno Peak",
      "Vega Square",
      "Route 05",
      "Thunderhead",
      "Basin",
      "Metro",
      "Caldera",
      "Project 9",
      "Haze",
      "Helios Descent",
      "Port Ares",
      "Kuiper Overturn"
    )

    val workshopLevels = List(
      "(POD) Galleria",
      "Abyssus",
      "Aeolus II",
      "Altima XIV",
      "Annapurna",
      "Antelao",
      "Apophis",
      "Aquilon II",
      "Atan-Ra",
      "Batur Crater",
      "Beltane",
      "Brigadoon",
      "Cairodrome II",
      "Cairodrome",
      "Canyonlands",
      "Carcosa",
      "Cassini Dome",
      "Cerridan VI",
      "Desolata",
      "Dione IV",
      "Druidia",
      "Eschaton",
      "Faradome",
      "Fractalos-324",
      "Gare d'Europa (WipEout2097)",
      "Gehennom",
      "Goldbridge",
      "Great Manitou Trail",
      "Helheim",
      "Kallisti IV",
      "Kamanneq",
      "Kanlaon Peak",
      "Kena Memorial",
      "Khayyam Road",
      "L-Damar",
      "Libohove",
      "Lion's Den",
      "Lumenar V",
      "M-Aero",
      "Manor Top (Wip3out)",
      "Mega Mall (Wip3out)",
      "Merkur Alpha",
      "Millennium Wharf",
      "Monakon-X",
      "Mount Ouragan",
      "Nakbe",
      "Nemesis",
      "Neo Praha",
      "Nitia Descent",
      "Nocturne",
      "Oblivion",
      "Oceanus",
      "Peikkosilta",
      "Pont des Anges",
      "Port Zvezh",
      "Pretoria Stadium",
      "Project Pandora",
      "Raven's Nest",
      "Rheinland",
      "Ruhr-02",
      "Runavik",
      "Saint Caomhan's Point",
      "Shanghai Financial",
      "Shimoda Run",
      "Shiva's Dance",
      "Silverthread",
      "Singadome",
      "Site-18",
      "South Ridge",
      "Sovereign",
      "Stanza Inter (Wip3out)",
      "Staten Towers",
      "Strelka Complex",
      "Sylvatar",
      "Taktsang",
      "Tamoanchan",
      "Ten Bears Point",
      "The Tir",
      "Tiamat's Maw",
      "Tsagaan Park",
      "Tupu Inca",
      "Valkyrie's Flight",
      "Vestfjorden",
      "Vostrukha I",
      "Vulcan's Forge",
      "Waddenzee",
      "Wellhead Raceway",
      "Willpower",
      "Xar Gate",
      "Yoru City",
      "Yukatek",
      "Yun Cheng Central",
      "YZ Indah",
    )

    def openCampaignForRace(name: String) =
      s"""
<Settings Name="$name" BarracudaAllowed="False" CustomShipsAllowed="False" Video="" FallbackTexture="" />
<Group PointsToUnlock="0">
"""

    val closeGroup = "</Group>"

    sealed trait SpeedClass
    case object Toxic extends SpeedClass
    case object Apex extends SpeedClass
    case object Halberd extends SpeedClass
    case object Spectre extends SpeedClass
    case object Zen extends SpeedClass

    sealed trait Difficulty
    case object Novice extends Difficulty
    case object Experienced extends Difficulty
    case object Expert extends Difficulty
    case object Elite extends Difficulty
    case object Hardcore extends Difficulty

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

    import java.nio.file.{Paths, Files}
    import java.nio.charset.StandardCharsets

    val useLevels = workshopLevels

    val singleRaceEvents = useLevels.map(l => race(l)).mkString
    val tournamentLevels = useLevels.map(l => s"""      <Level Name="$l" />""").mkString("\r\n")

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

    val fileName = "out_workshop.xml"
    Files.write(Paths.get(s"out/$fileName"), s.getBytes(StandardCharsets.UTF_8))

    // also write directly to game dir
    Files.write(Paths.get(s"D:/SteamLibrary/steamapps/common/BallisticNG/User/Mods/Campaigns/$fileName"), s.getBytes(StandardCharsets.UTF_8))

  }

}