package com.github.jacobbishopxy.theory.monads

/**
 * Created by jacob on 2/4/2020
 *
 * `cats.data.Writer` is a monad that let us carry a log along with a computation. We can use it to
 * record messages, errors, or additional data about a computation, and extract the log alongside
 * the final result.
 *
 * One common use for `Writers` is recording sequences of steps in multi-threaded computations where
 * standard imperative logging techniques can result in interleaved messages from different contexts.
 * With `Writer` the log for the computation is tied to the result, so we can run concurrent computations
 * without mixing logs.
 *
 * > Cats Data Types
 * >    Writer is the first data type we've seen from the cats.data package. This package provides
 * instances of various type classes that produce useful semantics.
 */
object TheWriterMonad {

  /**
   * 1. Creating and Unpacking Writers
   *
   * A `Writer[W, A]` carries two values: a log of type W and a result of type A. We can create `Writer`
   * from values of each type as follows:
   */

  import cats.data.Writer
  import cats.instances.vector._ // for Monoid

  Writer(Vector(
    "It was the best of times",
    "It was the worst of times"
  ), 1859)
  // cats.data.WriterT[cats.Id,scala.collection.immutable.Vector[String],Int] =
  // WriterT((Vector(It was the best of times, it was the worst of times),1859)

  /**
   * Notice that the type reported on the console is actually `WriterT[Id, Vector[String], Int]` instead
   * of `Writer[Vector[String], Int]` as we might expect. In the spirit of code reuse, Cats implements
   * Writer in terms of another type, `WriterT`. `WriterT` is an example of a new concept called a
   * `monad transformer`, which we will cover in the next chapter.
   *
   * Let's try to ignore this detail for now. `Writer` is a type alias for `WriterT`, so we can read types
   * like `WriterT[Id, W, A]` as `Writer[W, A]`: `type Writer[W, A] = WriterT[Id, W, A]`
   *
   * For convenience, Cats provides a way of creating `Writers` specifying only the log or the result.
   * If we only have a result we can use the standard pure syntax. To do this we must have a Monoid[W] in
   * scope so Cats knows how to produce an empty log:
   */

  import cats.instances.vector._ // for Monoid
  import cats.syntax.applicative._ // for pure

  type Logged[A] = Writer[Vector[String], A]

  123.pure[Logged]
  // WriterT(Vector(), 123)

  // If we have a log and no result we can create a `Writer[Unit]` using the tell syntax from
  // `cats.syntax.writer`:

  import cats.syntax.writer._

  Vector("msg1", "msg2", "msg3").tell
  // cats.data.Writer[scala.collection.immutable.Vector[String], Unit] =
  // WriterT((Vector(msg1, msg2, msg3),()))

  // If we have both a result and a log, we can either use `Writer.apply` or we can use the `writer`
  // syntax from `cats.syntax.writer`:

  val a = Writer(Vector("msg1", "msg2", "msg3"), 123)
  // a: cats.data.WriterT[cats.Id,scala.collection.immutable.Vector[String],Int] =
  // WriterT((Vector(msg1, msg2, msg3),123))

  val b = 123.writer(Vector("msg1", "msg2", "msg3"))
  // b: cats.data.Writer[scala.collection.immutable.Vector[String],Int] =
  // WriterT((Vector(msg1, msg2, msg3),123))

  val aResult = a.value // 123
  val bResult = b.value // Vector(msg1, msg2, msg3)

  // We can extract both values at the same time using the run method:
  val (log, reulst) = b.run

  /**
   * 2. Composing and Transforming Writers
   *
   * The log in a `Writer` is preserved when we `map` or `flatMap` over it. `flatMap` appends the
   * logs from the sourceWriter and the result of the user's sequencing function. For this reason
   * it's good practice to use a log type that has an efficient append and concatenate operations,
   * such as Vector:
   */

  val writer1 = for {
    a <- 10.pure[Logged]
    _ <- Vector("a", "b", "c").tell
    b <- 32.writer(Vector("x", "y", "z"))
  } yield a + b
  // cats.data.WriterT[cats.Id,Vector[String],Int] =
  // WriterT((Vector(a, b, c, x, y, z),42))

  val w1 = writer1.run
  // cats.Id[(Vector[String], Int)] = (Vector(a, b, c, x, y, z) ,42)

  // In addition to transforming the result with `map` and `flatMap`, we can transform the log in
  // a Writer with the `mapWritten` method:
  val writer2 = writer1.mapWritten(_.map(_.toUpperCase))
  // cats.data.WriterT[cats.Id,scala.collection.immutable.Vector[String],Int] =
  // WriterT((Vector(A, B, C, X, Y, Z),42))

  val w2 = writer2.run
  // cats.Id[(scala.collection.immutable.Vector[String], Int)] = (Vector(A, B, C, X, Y, Z),42)

  /**
   * We can transform both log and result simultaneously using `bimap` or `mapBoth`. `bimap` takes
   * two function parameters, one for the log and one for the result. `mapBoth` takes a single function
   * that accepts two parameters:
   */

  val writer3 = writer1.bimap(
    log => log.map(_.toUpperCase),
    res => res * 100
  )
  // cats.data.WriterT[cats.Id,scala.collection.immutable.Vector[String],Int] =
  // WriterT((Vector(A, B, C, X, Y, Z),4200))

  val w3 = writer3.run
  // cats.Id[(scala.collection.immutable.Vector[String], Int)] =
  // (Vector(A, B, C, X, Y, Z),4200)

  val writer4 = writer1.mapBoth { (log, res) =>
    val log2 = log.map(_ + "!")
    val res2 = res * 1000
    (log2, res2)
  }
  // cats.data.WriterT[cats.Id,scala.collection.immutable.Vector[String],Int] =
  // WriterT((Vector(a!, b!, c!, x!, y!, z!), 42000))

  val w4 = writer4.run
  //  cats.Id[(scala.collection.immutable.Vector[String], Int)] =
  //  (Vector(a!, b!, c!, x!, y!, z!),42000)

  val writer5 = writer1.reset
  // cats.data.WriterT[cats.Id,Vector[String],Int] = WriterT((Vector(),42))

  val w5 = writer5.run
  // cats.Id[(Vector[String], Int)] = (Vector(),42)

  val writer6 = writer1.swap
  // cats.data.WriterT[cats.Id,Int,Vector[String]] =
  // WriterT((42,Vector(a, b, c, x, y, z)))

  val w6 = writer6.run
  // cats.Id[(Int, Vector[String])] = (42,Vector(a, b, c, x, y, z))
}

object ExerciseShowYourWorking {

  /**
   * `Writers` are useful for logging operations in multi-threaded environments. Let's confirm this by
   * computing (and logging) some factorials.
   *
   * The factorial function below computes a factorial and prints out the intermediate steps as it runs.
   * The `slowly` helper function ensures this takes a while to run, even on the very small examples below:
   */

  def slowly[A](body: => A): A = try body finally Thread.sleep(100)

  def factorial(n: Int): Int = {
    val ans = slowly(if (n == 0) 1 else n * factorial(n - 1))
    println(s"fact $n $ans")
    ans
  }

  // Here's the output -- a sequence of monotonically increasing values:
  factorial(5)
  // fact 0 1
  // fact 1 1
  // fact 2 2
  // fact 3 6
  // fact 4 24
  // fact 5 120
  // Int = 120

  /**
   * If we start several factorials in parallel, the log messages can become interleaved on standard out.
   * This makes it difficult to see which messages come from which computation:
   */

  import scala.concurrent._
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._

  Await.result(Future.sequence(Vector(
    Future(factorial(3)),
    Future(factorial(3))
  )), 5.seconds)

  /**
   * Rewrite factorial so it captures the log messages in a `Writer`. Demonstrate that this allows us to
   * reliably the logs for concurrent computations.
   */

  import cats.data.Writer
  import cats.instances.vector._ // for Monoid
  import cats.syntax.writer._ // for tell
  import cats.syntax.applicative._ // for pure

  type Logged[A] = Writer[Vector[String], A]

  def factorialPro(n: Int): Logged[Int] =
    for {
      ans <- if (n == 0) 1.pure[Logged] else slowly(factorialPro(n - 1).map(_ * n))
      _ <- Vector(s"fact $n $ans").tell
    } yield ans

  // test

  val Vector((logA, ansA), (logB, ansB)) =
    Await.result(Future.sequence(Vector(
      Future(factorialPro(5).run),
      Future(factorialPro(3).run)
    )), 5.second)
}
