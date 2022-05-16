package gk.searchengine

import gk.searchengine.Scoring.{Rank, topRanks}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ScoringTest extends AnyFlatSpec with Matchers {

  "rank" should "be 1 if file contains all words" in {
    val words = "rank should be if file contains all words"
    val additionalWords = "other words"
    val content = (words + additionalWords).split(" ").map(_.hashCode).toSet
    val fileIndex = FileIndex("test", content)
    val searchTokens = Set(
      "rank", "should", "be", "all"
    )
    Scoring.rankFile(searchTokens, fileIndex).value shouldBe 1.0
  }

  it should "0 if file is empty" in {
    Scoring.rankFile(Set.empty, FileIndex("test", Set.empty)).value shouldBe 0.0
  }

  it should "be 0 if file does not contain any word" in {
    val content = "content of a text".split(" ").map(_.hashCode).toSet
    val fileIndex = FileIndex("test", content)
    val searchTokens = Set(
      "rank", "should", "be", "all"
    )
    Scoring.rankFile(searchTokens, fileIndex).value shouldBe 0.0
  }

  it should "be 0 < r < 1 if file does contain some of the words" in {
    val content = "content of a text".split(" ").map(_.hashCode).toSet
    val fileIndex = FileIndex("test", content)
    val searchTokens = Set(
      "content", "should", "be", "all"
    )
    Scoring.rankFile(searchTokens, fileIndex).value should be < 1.0
    Scoring.rankFile(searchTokens, fileIndex).value should be > 0.0
  }

  "topRanks" should "return limited/capped number of Ranks in descending order" in {
    val ranks = Range(1,10).map(index => Rank(index.toString, index)).toList
    val cap = 5
    topRanks(cap)(ranks) shouldBe ranks.reverse.take(cap)
  }
}
