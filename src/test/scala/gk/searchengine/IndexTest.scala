package gk.searchengine

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class IndexTest extends AnyFlatSpec with Matchers{
  private val fileIndexInput: FileIndexInput = FileIndexInput("name", Seq(
    "word",
    "org scalatest matchers should Matchers",
    ""
  ))

  private val index: FileIndex = Index.buildFileIndex(Tokenizer.getWords)(fileIndexInput)

  "buildFileIndex" should "build FileIndex with correct fileName" in {
    index.fileName shouldBe fileIndexInput.fileName
  }

  it should "build FileIndex containing all input words" in {
    fileIndexInput.lines
      .flatMap(Tokenizer.getWords)
      .foreach { word =>
        index.contains(word) shouldBe true
      }
  }

  it should "build FileIndex does not containing all input words" in {
    val wordsNotInInput = Seq("foo", "bar")
    wordsNotInInput.foreach { word =>
      index.contains(word) shouldBe false
    }
  }

  it should "build empty FileIndex for empty input" in {
    val index =Index.buildFileIndex(Tokenizer.getWords)(FileIndexInput("empty", Seq.empty))
    index.contains("word") shouldBe false
  }
}
