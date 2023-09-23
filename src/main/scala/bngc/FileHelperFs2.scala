package bngc

import bngc.Error._
import cats.data.ValidatedNel
import cats.effect.Sync
import cats.effect.std.Console
import cats.syntax.all._
import cats.{ApplicativeThrow, Monad}
import fs2.io.file.{Files, Path}
import fs2.Stream

import java.nio.file.InvalidPathException

object FileHelperFs2 {

  def validateFileIsReadable[F[_]: Files: Sync: ApplicativeThrow](
      s: String
  ): F[ValidatedNel[Error, Path]] = for {
    path <- validatePath(s)
    readable <- path.flatTraverse(fileIsReadable[F])
  } yield readable.toValidatedNel

  def validateIsDirectory[F[_]: Files: Sync: ApplicativeThrow](
      s: String
  ): F[ValidatedNel[Error, Path]] = for {
    path <- validatePath(s)
    readable <- path.flatTraverse(isDirectory[F])
  } yield readable.toValidatedNel

  def readAllLines[F[_]: Files: Sync](path: Path): F[List[String]] = {
    Files[F].readUtf8Lines(path).compile.toList
  }

  def readTemplate[F[_]: Files: Sync](name: String): F[String] = for {
    path <- path(s"src/main/resources/templates/$name.xml")
    lines <- Files[F].readUtf8Lines(path).compile.toList
  } yield lines.mkString("\r\n")

  def writeFile[F[_]: Sync: Console: Files](outDirPath: Path, outFileName: String, content: String): F[Unit] = {
    val path = outDirPath / Path(s"$outFileName.xml")
    for {
      _ <- Console[F].println(s"Writing campaign XML to [${path.absolute}]")
      _ <- Stream.emit(content).through(Files[F].writeUtf8(path)).compile.drain
    } yield ()
  }

  private def path[F[_]: Files: Sync](s: String): F[Path] = Sync[F]
    .catchOnly[InvalidPathException](java.nio.file.Paths.get(s))
    .map(Path.fromNioPath)

  private def validatePath[F[_]: Files: Sync: ApplicativeThrow](s: String): F[Either[Error, Path]] = {
    path(s)
      .map(Either.right[Error, Path])
      .recover { case _: InvalidPathException => Either.left(PathInvalid(s)) }
  }

  private def fileIsReadable[F[_]: Files: Monad](
      path: Path
  ): F[Either[Error, Path]] = Files[F]
    .isReadable(path)
    .ifF(
      Right(path),
      Left(FileNotReadable(path))
    )

  private def isDirectory[F[_]: Files: Monad](
      path: Path
  ): F[Either[Error, Path]] = Files[F]
    .isDirectory(path)
    .ifF(
      Right(path),
      Left(NotADirectory(path))
    )

}
