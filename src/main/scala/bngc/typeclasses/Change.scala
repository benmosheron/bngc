package bngc.typeclasses

import fs2.io.file.Path

trait Change[A, B] {
  def change(a: A): B
}
object Change {
  type ChangeFromString[B] = Change[String, B]
  type ChangeFromInt[B] = Change[Int, B]
  type ChangeFromPath[B] = Change[Path, B]

  object ChangeFromString {
    def apply[B: ChangeFromString]: ChangeFromString[B] = implicitly[ChangeFromString[B]]
  }

  object ChangeFromInt {
    def apply[B: ChangeFromInt]: ChangeFromInt[B] = implicitly[ChangeFromInt[B]]
  }

  object ChangeFromPath {
    def apply[B: ChangeFromPath]: ChangeFromPath[B] = implicitly[ChangeFromPath[B]]
  }
}
