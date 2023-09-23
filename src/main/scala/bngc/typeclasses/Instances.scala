package bngc.typeclasses

import bngc.PlainData._
import bngc.template.{MainTemplate, SingleRaceEventTemplate, TournamentLevelTemplate}
import fs2.io.file.Path
import bngc.typeclasses.Change._

object Instances {

  implicit object ChangePathToLevelFilePath extends ChangeFromPath[LevelFilePath] {
    override def change(path: Path): LevelFilePath = LevelFilePath(path)
  }

  implicit object ChangePathToOutDirPath extends ChangeFromPath[OutDirPath] {
    override def change(path: Path): OutDirPath = OutDirPath(path)
  }

  implicit object ChangeStringToLevelName$ extends ChangeFromString[LevelName] {
    override def change(a: String): LevelName = LevelName(a)
  }

  implicit object ChangeStringToMainTemplate$ extends ChangeFromString[MainTemplate] {
    override def change(a: String): MainTemplate = MainTemplate(a)
  }

  implicit object ChangeStringToSingleRaceEventTemplate$ extends ChangeFromString[SingleRaceEventTemplate] {
    override def change(a: String): SingleRaceEventTemplate = SingleRaceEventTemplate(a)
  }

  implicit object LiftStringToTournamentLevelTemplate extends ChangeFromString[TournamentLevelTemplate] {
    override def change(a: String): TournamentLevelTemplate = TournamentLevelTemplate(a)
  }

}
