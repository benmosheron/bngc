package bngc

sealed trait Difficulty
case object Novice extends Difficulty
case object Experienced extends Difficulty
case object Expert extends Difficulty
case object Elite extends Difficulty
case object Hardcore extends Difficulty
