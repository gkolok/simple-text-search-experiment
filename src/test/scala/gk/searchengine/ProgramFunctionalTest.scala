package gk.searchengine

import gk.searchengine.io.{Console, TestConsole}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ProgramFunctionalTest  extends AnyFlatSpec with Matchers {
  import Program._

  "program" should "print error message if no argument is passed" in {
    implicit val testConsole: TestConsole = TestConsole()
    io.Runtime.run(program(Array.empty))
    testConsole.linesWritten shouldBe Array("Missing path argument.")
  }

  it should "print error message if directory does not exist" in {
    implicit val testConsole: TestConsole = TestConsole()
    io.Runtime.run(program(Array("non-existing-directory")))
    testConsole.linesWritten shouldBe Array("Path [non-existing-directory] is not a directory")
  }

  it should "print file processing error message, # of files indexed, ranks" in {
    implicit val testConsole: TestConsole = TestConsole("error bbs")
    io.Runtime.run(program(Array("testdata")))
    testConsole.linesWritten shouldBe Array(
      "Failed to index file: pdf.txt, exception: java.nio.charset.MalformedInputException: Input length = 1",
      "Failed to index file: pdf2.txt, exception: java.nio.charset.MalformedInputException: Input length = 1",
      "Indexed 4 files",
      "search> ethics.txt: 100%",
      "howtobbs.txt: 50%",
      "search> "
    )
  }
}
