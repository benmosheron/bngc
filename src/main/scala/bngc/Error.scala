package bngc

object Error {
  sealed trait Error
  case class FileNotReadable(path: String) extends Error
  case class InvalidSpeedClass(invalid: String) extends Error
  case class InvalidDifficulty(invalid: String) extends Error
}
