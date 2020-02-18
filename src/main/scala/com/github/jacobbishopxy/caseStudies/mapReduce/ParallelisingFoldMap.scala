package com.github.jacobbishopxy.caseStudies.mapReduce

/**
 * Created by Administrator on 2/17/2020
 */
object ParallelisingFoldMap {

  /**
   * Now we have a working single-threaded implementation of `foldMap`, let's look at distributing work to run in
   * parallel. We'll use our single-threaded version of foldMap as a building block.
   *
   * We'll write a multi-CPU implementation that simulates the way we would distribute work in a map-reduce cluster:
   *
   * a. we start with an initial list of all the data we need to process;
   * b. we divide the data into batches, sending one batch to each CPU;
   * c. the CPUs run a batch-level map phase in parallel;
   * d. the CPUs run a batch-level reduce phase in parallel, producing a local result for each batch;
   * e. we reduce the results for each batch to a single final result.
   */

  /**
   * 1. Futures, Thread Pools, and ExecutionContexts
   *
   * We already know a fair amount about the monadic nature of `Futures`. Let's take a moment for a quick recap, and
   * to describe how Scala futures are scheduled behind the scenes.
   *
   * `Future`s run on a thread pool, determined by an implicit ExecutionContext parameter. Whenever we create a
   * `Future`, whether through a call to `Future.apply` or some other combinator, we must have an implicit
   * ExecutionContext in scope:
   */

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.Future

  val future1: Future[Int] = Future {
    (1 to 100).toList.foldLeft(0)(_ + _)
  }

  val future2: Future[Int] = Future {
    (100 to 200).toList.foldLeft(0)(_ + _)
  }

  /**
   * In this example we've imported a `ExecutionContext.Implicits.global`. This default context allocates a thread
   * pool with one thread per CPU in our machine. When we create a Future the ExecutionContext schedules it for
   * execution. If there is a free thread in the pool, the Future starts executing immediately. Most modern machines
   * have at least two CPUs, so in our example it is likely that `future1` and `future2` will execute in parallel.
   */

  /**
   * Some combinators create new `Future`s that schedule work based on the results of other `Future`s. The `map` and
   * `flatMap` methods, for example, schedule computations that run as soon as their input values are computed and
   * a CPU is available:
   */

  val future3: Future[String] = future1.map(_.toString)

  val future4: Future[Int] = for {
    a <- future1
    b <- future2
  } yield a + b

  // We can convert a `List[Future[A]]` to a `Future[List[A]]` using `Future.sequence`:

  Future.sequence(List(Future(1), Future(2), Future(3)))

  // or an instance of `Traverse`:

  import cats.instances.future._
  import cats.instances.list._
  import cats.syntax.traverse._ // for sequence

  List(Future(1), Future(2), Future(3)).sequence

  // An `ExecutionContext` is required in either case. Finally, we can use `Await.result` to block on a `Future` until
  // a result is available:

  import scala.concurrent._
  import scala.concurrent.duration._

  Await.result(Future(1), 1.second) // wait for the result

  // There are also Monad and Monoid implementations for `Future` available from `cats.instances.future`:

  import cats.instances.future._
  import cats.instances.int._
  import cats.{Monad, Monoid} // for Monad and Monoid

  Monad[Future].pure(42)
  Monoid[Future[Int]].combine(Future(1), Future(2))
}

object DividingWork {

  /**
   * 2. Dividing Work
   *
   * Now we've refreshed our memory of `Futures`, let's look at how we can divide work into batches. We can query the
   * number of available CPUs on our machine using an API call from the Java standard library:
   */

  Runtime.getRuntime.availableProcessors

  /**
   * We can partition a sequence (actually anything that implements `Vector`) using the `grouped` method. We'll use
   * this to split off batches of work for each CPU:
   */

  (1 to 10).toList.grouped(3).toList
}

object ImplementingParallelFoldMap {

  /**
   * 3. Implementing parallelFoldMap
   *
   * Implement a parallel version of `foldMap` called `parallelFoldMap`. Here is the type signature:
   */

  import cats.Monoid
  import cats.syntax.semigroup._ // |+|

  import scala.concurrent.{Await, ExecutionContext, Future}


  def parallelFoldMap0[A, B: Monoid](values: Vector[A])(func: A => B): Future[B] = ???

  /**
   * Use the techniques described above to split the work into batches, one batch per CPU. Process each batch in a
   * parallel thread.
   *
   * For bonus points, process the batches for each CPU using your implementation of foldMap from above.
   */

  import ImplementingFoldMap.foldMap

  def parallelFoldMap[A, B: Monoid](values: Vector[A])(func: A => B)
                                   (implicit ect: ExecutionContext): Future[B] = {
    // Calculate the number of items to pass to each CPU:
    val cores = Runtime.getRuntime.availableProcessors
    val size = (values.size.toDouble / cores).ceil.toInt

    // Create one group for each CPU:
    val groups = values.grouped(size)

    // Create a future to foldMap each group:
    val futures: Iterator[Future[B]] = groups.map { g =>
      Future { foldMap(g, func) }
    }

    // foldMap over the groups to calculate a final result:
    Future.sequence(futures).map { iter =>
      iter.foldLeft(Monoid[B].empty)(_ |+| _)
    }
  }


  // test

  import cats.instances.int._ // Monoid[Int]

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._

  val result: Future[Int] = parallelFoldMap((1 to 1000000).toVector)(identity)
  Await.result(result, 1.second)

}

object ParallelFoldMapWithMoreCats {

  /**
   * 4. parallelFoldMap with more Cats
   *
   * Although we implemented `foldMap` ourselves above, the method is also available as part of the `Foldable` type
   * class. Reimplement `parallelFoldMap` using Cats' `Foldable` and `Traversable` type class.
   */

  import cats.Monoid
  import cats.instances.future._ // Applicative and Monad
  import cats.instances.vector._ // Foldable and Traverse
  import cats.syntax.foldable._
  import cats.syntax.traverse._

  import scala.concurrent.{ExecutionContext, Future, Await}

  def parallelFoldMap[A, B: Monoid](values: Vector[A])(func: A => B)
                                   (implicit ect: ExecutionContext): Future[B] = {

    val cores = Runtime.getRuntime.availableProcessors
    val size = (values.size.toDouble / cores).ceil.toInt

    values
      .grouped(size)
      .toVector
      .traverse(g => Future(g.foldMap(func)))
      .map(_.combineAll)
  }


  // test

  import cats.instances.int._ // Monoid[Int]
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._

  val result: Future[Int] = parallelFoldMap((1 to 100000).toVector)(_ * 1000)

  Await.result(result, 1.second)
}
