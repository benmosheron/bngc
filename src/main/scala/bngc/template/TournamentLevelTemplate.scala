package bngc.template

import bngc.Placeholder.StringUtil
import bngc.PlainData.LevelName

final case class TournamentLevelTemplate(s: String) {
  def withLevelName(levelName: LevelName) = TournamentLevelTemplate(s.withLevelName(levelName))
}
