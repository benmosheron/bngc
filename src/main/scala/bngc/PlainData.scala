package bngc

import fs2.io.file.Path

object PlainData {

  final case class LevelFilePath(path: Path)
  final case class OutDirPath(path: Path)
  final case class CampaignName(s: String)
  final case class Points(i: Int)
  final case class LevelName(s: String)

}
