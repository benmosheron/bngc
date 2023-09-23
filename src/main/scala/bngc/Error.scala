package bngc

import fs2.io.file.Path

object Error {
  sealed trait Error
  case class PathInvalid(invalid: String) extends Error
  case class FileNotReadable(path: Path) extends Error
  case class InvalidSpeedClass(invalid: String) extends Error
  case class InvalidDifficulty(invalid: String) extends Error
  case class IsNotInteger(invalid: String) extends Error
  case class NotADirectory(path: Path) extends Error
}
