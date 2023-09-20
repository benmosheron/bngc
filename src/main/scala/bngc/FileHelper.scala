package bngc

import bngc.Error._
import cats.data.Validated.{Invalid, Valid}
import cats.{Applicative, Monad}
import cats.data.ValidatedNel
import cats.effect.std.Console
import cats.syntax.all._

import java.nio.charset.StandardCharsets
import scala.jdk.CollectionConverters._
import java.nio.file.{Files, Paths}

object FileHelper {

  def validateFileIsReadable[F[_]: Monad](
      filePath: String
  ): F[ValidatedNel[Error, Unit]] = for {
    path <- Applicative[F].pure(Paths.get(filePath))
    exists <- Applicative[F].pure(Files.isReadable(path))
    result <-
      if (exists) {
        Applicative[F].pure(Valid(()))
      } else {
        Applicative[F].pure(
          Invalid(FileNotReadable(filePath)).toValidatedNel
        )
      }
  } yield result

  def validateDirExists[F[_]: Monad](
      dirPath: String
  ): F[ValidatedNel[Error, Unit]] = for {
    path <- Applicative[F].pure(Paths.get(dirPath))
    exists <- Applicative[F].pure(Files.isDirectory(path))
    result <-
      if (exists) {
        Applicative[F].pure(Valid(()))
      } else {
        Applicative[F].pure(Invalid(DirectoryDoesNotExist(dirPath)).toValidatedNel)
      }
  } yield result

  def readLines[F[_]: Monad: Console](filePath: String): F[List[String]] = for {
    path <- Monad[F].pure(Paths.get(filePath))
    lines <- Monad[F].pure(Files.readAllLines(path).asScala.toList)
    _ <- Console[F].println(s"Read [${lines.size}] level names from file")
    _ <- lines.map(line => Console[F].println(s"* $line")).sequence_
  } yield lines

  def readTemplate[F[_]: Applicative](name: String): F[String] =
    Applicative[F].pure(Files.readString(Paths.get(s"src/main/resources/templates/$name.xml")))

  def writeFile[F[_]: Monad: Console](filePath: String, content: String): F[Unit] = for {
    path <- Applicative[F].pure(Paths.get(filePath))
    _ <- Console[F].println(s"Writing campaign XML to [${path.toAbsolutePath.toString}]")
    _ <- Applicative[F].pure(Files.write(path, content.getBytes(StandardCharsets.UTF_8)))
  } yield ()
}
