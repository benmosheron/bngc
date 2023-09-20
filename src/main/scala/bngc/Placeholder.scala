package bngc

import bngc.adt.Difficulty._
import bngc.adt.SpeedClass._

object Placeholder {

  private val SingleRaceEvents = """<SingleRaceEvents />"""
  private val TournamentLevels = """<TournamentLevels />"""

  private val CampaignName = "#CampaignName"
  private val LevelName = "#LevelName"
  private val PointsToUnlockTournament = "#PointsToUnlockTournament"
  private val SpeedClass = "#SpeedClass"
  private val Difficulty = "#Difficulty"

  implicit class StringUtil(s: String) {

    def withSingleRaceEvents(singleRaceEvents: String): String = s.replace(SingleRaceEvents, singleRaceEvents)
    def withTournamentLevels(tournamentLevels: String): String = s.replace(TournamentLevels, tournamentLevels)

    def withCampaignName(campaignName: String): String =
      s.replace(CampaignName, campaignName)
    def withLevelName(levelName: String): String =
      s.replace(LevelName, levelName)
    def withPointsToUnlockTournament(pointsToUnlockTournament: Int): String =
      s.replace(
        PointsToUnlockTournament,
        pointsToUnlockTournament.toString
      )
    def withSpeedClass(speedClass: SpeedClass): String =
      s.replace(SpeedClass, speedClass.toString)
    def withDifficulty(difficulty: Difficulty): String =
      s.replace(Difficulty, difficulty.toString)

  }

}
