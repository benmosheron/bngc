package bngc.template

import bngc.Placeholder.StringUtil
import bngc.PlainData.{CampaignName, Points}
import bngc.adt.Difficulty.Difficulty
import bngc.adt.SpeedClass.SpeedClass

final case class MainTemplate(s: String) {

  def withSpeedClass(speedClass: SpeedClass) = MainTemplate(s.withSpeedClass(speedClass))
  def withDifficulty(difficulty: Difficulty) = MainTemplate(s.withDifficulty(difficulty))
  def withCampaignName(campaignName: CampaignName) = MainTemplate(s.withCampaignName(campaignName))
  def withPointsToUnlockTournament(points: Points) = MainTemplate(s.withPointsToUnlockTournament(points))
  def withSingleRaceEvents(singleRaceEvents: String) = MainTemplate(s.withSingleRaceEvents(singleRaceEvents))
  def withTournamentLevels(tournamentLevels: String) = MainTemplate(s.withTournamentLevels(tournamentLevels))

}
