package gk.searchengine

import gk.searchengine.Index.buildIndex
import gk.searchengine.Scoring.Rank
import gk.searchengine.io.TestConsole
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.{Failure, Success, Try}

class ProgramTest extends AnyFlatSpec with Matchers {
  import Program._
  import gk.searchengine.io.Runtime
  private val fileIndex = Index.buildFileIndex(Tokenizer.getWords) _

  "writeRank" should "print out file name and score" in {
    implicit val testConsole: TestConsole = TestConsole()
    val rank = Rank("fileName", 0.314)
    Runtime.run(writeRank(rank))
    testConsole.linesWritten shouldBe Array("fileName: 31%")
  }

  "writeRanks" should "print out no matches for empty ranks" in {
    implicit val testConsole: TestConsole = TestConsole()
    val ranks = List.empty
    Runtime.run(writeRanks(ranks))
    testConsole.linesWritten shouldBe Array("no matches found")
  }

  "writeRanks" should "print out multiple scores" in {
    implicit val testConsole: TestConsole = TestConsole()
    val ranks = List(
      Rank("1", 0.03),
      Rank("2", 0.4),
      Rank("3", 0.9)
    )
    Runtime.run(writeRanks(ranks))
    testConsole.linesWritten shouldBe Array(
      "1:  3%",
      "2: 40%",
      "3: 90%"
    )
  }

  "processLines" should "print out file read errors, # of indexed files" in {
    implicit val testConsole: TestConsole = TestConsole("one two four")
    val lines: Iterator[(String, Try[Seq[String]])] = Seq(
      "file1" -> Failure(new RuntimeException("Error reading file1")),
      "file2" -> Failure(new RuntimeException("Error reading file2")),
      "file3" -> Success(Seq("one two three"))
    ).iterator
    Runtime.run(processLines("fakePath", lines))
    testConsole.linesWritten shouldBe Array(
      "Failed to index file: file1, exception: java.lang.RuntimeException: Error reading file1",
      "Failed to index file: file2, exception: java.lang.RuntimeException: Error reading file2",
      "1 files read in directory: fakePath",
      "search> file3: 67%",
      "search> ")
  }

  "iterate" should "print out search search>" in {
    implicit val testConsole: TestConsole = TestConsole()
    val index = buildIndex()
    Runtime.run(iterate(index))
    testConsole.linesWritten shouldBe Array("search> ")
  }

  it should "no match found" in {
    implicit val testConsole: TestConsole = TestConsole("foo bar")
    val fileIndexInput = FileIndexInput("test", Seq.empty)
    val index = buildIndex(fileIndex(fileIndexInput))
    Runtime.run(iterate(index))
    testConsole.linesWritten shouldBe Array(
      "search> no matches found",
      "search> "
    )
  }

  it should "not search/find :quit and should not search words after quit" in {
    implicit val testConsole: TestConsole = TestConsole("foo", ":quit", "word")
    val fileInput = fileIndexInput(content = ":quit", "quit", "word", "foo")
    val index = buildIndex(fileIndex(fileInput))
    Runtime.run(iterate(index))
    testConsole.linesWritten shouldBe Array(
      "search> test: 100%", // match for "foo"
      "search> "            // :quit
                            // exit, no find for "word"
    )
  }

  it should "print scores in descending order" in {
    implicit val testConsole: TestConsole = TestConsole("1 2 3" )
    val index = buildIndex(
      fileIndex(fileIndexInput(name = "1", content = "1")),
      fileIndex(fileIndexInput(name = "12", content = "1", "2")),
      fileIndex(fileIndexInput(name = "123", content = "1", "2","3")),
    )
    Runtime.run(iterate(index))
    testConsole.linesWritten shouldBe Array(
      "search> 123: 100%",
      "12: 67%",
      "1: 33%",
      "search> "
    )
  }

  def fileIndexInput(content: String*) = FileIndexInput("test", content)

  def fileIndexInput(name: String, content: String*) = FileIndexInput(name, content)
}
