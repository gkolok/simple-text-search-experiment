package gk.searchengine

object Scoring {

  case class Rank(fileName: String, value: Double)

  def rankFile(words: Set[String], fileIndex: FileIndex): Rank = {
    val sum = words.toSeq.map { word =>
      if (fileIndex.contains(word)) 1.0 else 0.0
    }.sum
    val value = if (sum == 0.0) 0.0
    else if (words.isEmpty) 1.0
    else sum / words.size
    Rank(fileIndex.fileName, value)
  }

  def rankIndex(words: Set[String], index: Index): Seq[Rank] =
    index.indexes.map(rankFile(words, _))

  def topRanks(cap: Int)(ranks: List[Rank]): List[Rank] = ranks
    .sortBy(_.value)
    .reverse
    .take(cap)
    .filter(_.value > 0)
}
