package gk.searchengine.io

sealed trait IO[A] {
  def flatMap[B](f: A => IO[B]): IO[B] = IO.Compose(this, f)
  def map[B](f: A => B): IO[B] = flatMap(a => IO.succeed(f(a)))
}

object IO {
  case class Effect[A](f: () => A) extends IO[A]
  case class Fail[A](e: Throwable) extends IO[A]
  case class Compose[A, B](io: IO[A], f: A => IO[B]) extends IO[B]

  def succeed[A](a: A): IO[A] = Effect(() => a)
  def apply[A](a: => A): IO[A] = Effect(() => a)
  def sequence[A](s: List[IO[A]]): IO[List[A]] = {
    val empty: IO[List[A]] = Effect(() => List.empty[A])
    s.foldLeft(empty) {
      case (acc, io) => for {
        l <- acc
        a <- io
      } yield a :: l
    }
  }
}

trait Console {
  def write(s: String): IO[Unit]
  def writeln(s: String): IO[Unit]
  def readLine(): IO[Option[String]]
}

class DefaultConsole extends Console {
  override def write(s: String): IO[Unit] = IO(print(s))
  override def writeln(s: String): IO[Unit] = IO(println(s))
  override def readLine(): IO[Option[String]] = IO(Option(io.StdIn.readLine()))
}




