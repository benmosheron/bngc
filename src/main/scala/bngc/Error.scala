package bngc

import fs2.io.file.Path

object Error {
  sealed trait Error
  final case class PathInvalid(invalid: String) extends Error
  final case class FileNotReadable(path: Path) extends Error
  final case class InvalidSpeedClass(invalid: String) extends Error
  final case class InvalidDifficulty(invalid: String) extends Error
  final case class InvalidHandleMultiple(invalid: String) extends Error
  final case class IsNotInteger(invalid: String) extends Error
  final case class NotADirectory(path: Path) extends Error
}
