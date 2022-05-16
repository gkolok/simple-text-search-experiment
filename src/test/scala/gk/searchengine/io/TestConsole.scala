package gk.searchengine.io

import java.io.{BufferedReader, StringReader}

class TestConsole(linesToRead: Seq[String]) extends Console {
  private val lineReader = new BufferedReader(new StringReader(linesToRead.mkString("\n")))
  private val stringBuilder = new StringBuilder()

  override def write(s: String): IO[Unit] = IO(stringBuilder.append(s))

  override def writeln(s: String): IO[Unit] = IO(stringBuilder.append(s + "\n"))

  override def readLine(): IO[Option[String]] = IO(Option(lineReader.readLine()))

  def linesWritten(): Array[String] = stringBuilder.result().split('\n')
}

object TestConsole {
  def apply(linesToRead: String*) = new TestConsole(linesToRead)
}