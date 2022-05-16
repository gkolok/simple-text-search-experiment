package gk.searchengine

object Tokenizer {
  type Tokenizer = CharSequence => Iterator[String]

  private val wordRegexp = """([A-Za-z0-9])+""".r
  def getWords(line: CharSequence): Iterator[String] = wordRegexp
    .findAllIn(line)
    .map(word => word.toLowerCase)
}
