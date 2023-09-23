package bngc.template

import bngc.Placeholder.StringUtil
import bngc.PlainData.LevelName
import bngc.adt.Difficulty.Difficulty
import bngc.adt.SpeedClass.SpeedClass

final case class SingleRaceEventTemplate(s: String) {
  def withLevelName(levelName: LevelName) = SingleRaceEventTemplate(s.withLevelName(levelName))
  def withSpeedClass(speedClass: SpeedClass) = SingleRaceEventTemplate(s.withSpeedClass(speedClass))
  def withDifficulty(difficulty: Difficulty) = SingleRaceEventTemplate(s.withDifficulty(difficulty))
}
