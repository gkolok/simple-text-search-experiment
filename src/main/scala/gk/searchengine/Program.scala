package gk.searchengine

import gk.searchengine.Scoring.{Rank, topRanks}
import gk.searchengine.io.{Console, IO}
import gk.searchengine.io.IO.{Compose, sequence, succeed}

import java.io.File
import java.nio.file.Files
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try, Using}

object Program {
  val TOP_RANKS_PRINTED = 10

  sealed trait ReadDirectoryError

  case object MissingPathArg extends ReadDirectoryError {
    override def toString: String = "Missing path argument."
  }

  case class NotDirectory(error: String) extends ReadDirectoryError {
    override def toString: String = error
  }

  case class FileReadError(error: String) extends ReadDirectoryError {
    override def toString: String = error
  }

  case class DirectoryNotFound(t: Throwable) extends ReadDirectoryError

  def program(args: Array[String])
             (implicit console: Console): IO[Unit] = Program
    .readDirectory(args)
    .fold(
      writeDirectoryError,
      processDirectory
    )

  def processDirectory(dir: File)
                      (implicit console: Console): IO[Unit] = {
    val lines = getFiles(dir).map(getLines)
    processLines(dir.getAbsolutePath, lines)
  }


  def processLines(directoryPath: String, lines: Iterator[(String, Try[Seq[String]])])
                          (implicit console: Console): IO[Unit] = {
    val (errors, fileIndexInputs) = partitionErrorsAndFileIndexInputs(lines)
    val fileIndexes = fileIndexInputs
      .map(Index.buildFileIndex(Tokenizer.getWords))
      .toSeq
    for {
      _ <- writeFileReadErrors(console, errors)
      _ <- console.writeln(s"${fileIndexes.size} files read in directory: $directoryPath")
      _ <- iterate(Index(fileIndexes))
    } yield ()
  }

  private def writeFileReadErrors(console: Console, errors: Iterator[FileReadError]): IO[List[Unit]] =
    sequence(errors.map( error => console.writeln(error.toString)).toList)

  private def partitionErrorsAndFileIndexInputs(lines: Iterator[(String, Try[Seq[String]])]): (Iterator[FileReadError], Iterator[FileIndexInput]) = {
    val (errors, fileIndexInputs) = lines
      .map(toEither)
      .partition(_.isLeft)
    (
      errors.collect { case Left(fileReadError) => fileReadError },
      fileIndexInputs.collect { case Right(fileIndexInput: FileIndexInput) => fileIndexInput }
    )
  }

  def writeDirectoryError(error: ReadDirectoryError)
                         (implicit console: Console): IO[Unit] =
    console.writeln(error.toString)

  def toEither(input: (String, Try[Seq[String]])): Either[FileReadError, FileIndexInput] =
    input match {
      case (fileName, Failure(exception)) => Left(FileReadError(s"Failed to index file: $fileName, exception: $exception"))
      case (fileName, Success(stringIterator)) => Right(FileIndexInput(fileName, stringIterator))
    }

  def readDirectory(args: Array[String]): Either[ReadDirectoryError, File] = {
    for {
      path <- args.headOption.toRight(MissingPathArg)
      directory <- Try(new File(path)).fold(
        throwable => Left(DirectoryNotFound(throwable)),
        file =>
          if (file.isDirectory) Right(file)
          else Left(NotDirectory(s"Path [$path] is not a directory"))
      )
    } yield directory
  }

  def getFiles(directory: File): Iterator[File] = {
    Files.list(directory.toPath).iterator.asScala
      .filter(path => path.getFileName.toString.endsWith(".txt"))
      .map(_.toFile)
  }

  def getLines(file: File): (String, Try[Seq[String]]) =
    (file.getName, Using(scala.io.Source.fromFile(file)) { source =>
      source.getLines().toSeq
    })

  def iterate(indexedFiles: Index)
             (implicit console: Console): IO[Unit] =
    for {
      _ <- console.write(s"search> ")
      lineOption <- console.readLine().map(_.filter(!_.equals(":quit")))
      wordsOption = lineOption
        .map(Tokenizer.getWords)
        .map(_.toSet)
      ranksOption = wordsOption.map(Scoring.rankIndex(_, indexedFiles))
      _ <- ranksOption.map(xs => writeRanks(topRanks(TOP_RANKS_PRINTED)(xs.toList))).getOrElse(IO.succeed())
      _ <- lineOption.map(_ => iterate(indexedFiles)).getOrElse(IO.succeed(()))
    } yield ()

  def writeRanks(ranks: List[Rank])
                (implicit console: Console): IO[Unit] =
    if (ranks.isEmpty) {
      console.writeln("no matches found")
    } else {
      for {
        _ <- sequence(ranks.map(rank => writeRank(rank)))
      } yield ()
    }

  def writeRank(rank: Rank)
               (implicit console: Console): IO[Unit] = {
    val percent = rank.value * 100.0
    console.writeln(f"${rank.fileName}: $percent%2.0f%%")
  }
}
