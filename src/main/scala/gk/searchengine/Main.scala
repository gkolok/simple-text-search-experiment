package gk.searchengine

import gk.searchengine.io.DefaultConsole

object Main extends App {
  val program = Program.program(args)(new DefaultConsole)
  io.Runtime.run(program)
}
