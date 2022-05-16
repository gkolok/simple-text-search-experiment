package gk.searchengine.io

import gk.searchengine.io.IO.Fail

import scala.util.{Failure, Success, Try}

object Runtime {
  def run[A](io: IO[A]): Try[A] = eval(io)

  private def eval[A](io: IO[A]): Try[A] = {
    io match {
      case IO.Effect(computation) => Try(computation())

      case IO.Compose(io, f: (Any => IO[A])) => eval(io) match {
        case Success(res) => eval(f(res))
        case Failure(e) => Failure(e)
      }

      case Fail(e) => Failure(e)
    }
  }
}