package gk.searchengine

import gk.searchengine.Tokenizer.Tokenizer

import java.io.File

case class FileProcessingError(file: File, t: Throwable)

case class FileIndex(fileName: String, setOfWordHashes: Set[Int]) {
  def contains(word: String): Boolean = setOfWordHashes.contains(word.hashCode)
}

case class FileIndexInput(fileName: String, lines: Seq[String])

case class Index(indexes: Seq[FileIndex]) {
  val numberOfFiles: Int = indexes.size
}

object Index {
  def buildFileIndex(tokenizer: Tokenizer)(fileIndexInput: FileIndexInput): FileIndex = {
    val setOfHash = fileIndexInput.lines.flatMap(tokenizer)
      .map(_.hashCode)
      .toSet
    FileIndex(fileIndexInput.fileName, setOfHash)
  }

  def buildIndex(fileIndexes: FileIndex*): Index = Index(fileIndexes)
}
