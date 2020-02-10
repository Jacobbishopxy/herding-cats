package com.github.jacobbishopxy.theory.foldableAndTraverse

/**
 * Created by jacob on 2/10/2020
 *
 * `foldLeft` and `foldRight` are flexible iteration methods but they requires us to do a lot of work to define
 * accumulators and combinator functions. The `Traverse` type class is a higher level tool that leverages
 * `Applicatives` to provide a more convenient, more lawful, pattern for iteration.
 */
object TraverseTypeClass {

  /**
   * 1. Traversing with Futures
   *
   * We can demonstrate `Traverse` using the `Future.traverse` and `Future.sequence` methods in the Scala standard
   * library. These methods provide Future-specific implementations of the traverse pattern. As an example, suppose
   * we have a list of server hostname and a method to poll a host for its uptime:
   */

  import scala.concurrent._
  import scala.concurrent.duration._
  import scala.concurrent.ExecutionContext.Implicits.global

  val hostnames = List(
    "alpha.example.com",
    "beta.example.com",
    "gamma.demo.com",
  )

  def getUptime(hostname: String): Future[Int] =
    Future(hostname.length * 60)

  /**
   * Now, suppose we want to poll all of the hosts and collect all of their uptimes. We can't simply map over
   * hostnames because the result -- a `List[Future[Int]]` -- would contain more than one `Future`. We need to
   * reduce the results to a single `Future` to get something we can block on. Let's start by doing this manually
   * using a fold:
   */

  val allUptimes: Future[List[Int]] =
    hostnames.foldLeft(Future(List.empty[Int])) { (acc, host) =>
      val uptime = getUptime(host)
      for {
        ac <- acc
        ut <- uptime
      } yield ac :+ ut
    }

  Await.result(allUptimes, 1.second) // List(1020, 960, 840)

  /**
   * This is much clearer and more concise -- let's see how it works. If we ignore distractions like `CanBuildFrom`
   * and `ExecutionContext`, the implementation of `Future.traverse` in the standard library looks like this:
   */

  def traverseMock[A, B](values: List[A])(func: A => Future[B]): Future[List[B]] =
    values.foldLeft(Future(List.empty[B])) { (acc, host) =>
      val item = func(host)
      for {
        ac <- acc
        it <- item
      } yield ac :+ it
    }

  /**
   * This is essentially the same as our example code above. `Future.traverse` is abstracting away the pain of folding
   * and defining accumulators and combination functions. It gives us a clean high-level interface to do what we want:
   *
   * - start with a `List[A]`;
   * - provide a function `A => Future[B]`;
   * - end up with a `Future[List[B]]`
   *
   * The standard library also provides another method, `Future.sequence`, that assumes we're starting with a
   * `List[Future[B]]` and don't need to provide an identity function:
   *
   * `
   * object Future {
   * def sequence[B](futures: List[Future[B]]): Future[List[B]] =
   * traverse(futures)(identity)
   * // etc...
   * }
   * `
   *
   * In this case the intuitive understanding is even simpler:
   *
   * - start with a `List[Future[A]]`;
   * - end up with a `Future[List[A]]`.
   *
   * `Future.traverse` and `Future.sequence` solve a very specific problem: they allow us to iterate over a sequence of
   * `Futures` and accumulate a result. The simplified examples above only work with `List`s, but the real
   * `Future.traverse` and `Future.sequence` work with any standard Scala collection.
   *
   * Cats' `Traverse` type class generalises these patterns to work with any type of Applicative: `Future`, `Option`,
   * `Validated`, and so on. We'll approach `Traverse` in the next sections in two steps: first we'll generalise over
   * the Applicative, then we'll generalise over the sequence type. We'll end up with an extremely valuable tool that
   * trivialises many operations involving sequences and other data types.
   */

  /**
   * 2. Traversing with Applicatives
   *
   * If we squint, we'll see that we can rewrite `traverse` in terms of an `Applicative`. Our accumulator from the
   * example above:
   *
   * `Future(List.empty[Int])`
   *
   * is equivalent to `Applicative.pure`:
   */

  import cats.Applicative
  import cats.instances.future._ // for Applicative
  import cats.syntax.applicative._ // for pure

  List.empty[Int].pure[Future]

  // Our combinator, which used to be this:

  def oldCombine(accum: Future[List[Int]], host: String): Future[List[Int]] = {
    val uptime = getUptime(host)
    for {
      ac <- accum
      ut <- uptime
    } yield ac :+ ut
  }

  // is now equivalent to `Semigroupal.combine`:

  import cats.syntax.apply._ // for mapN

  // Combining accumulator and hostname using an Applicative:
  def newCombine(accum: Future[List[Int]], host: String): Future[List[Int]] =
    (accum, getUptime(host)).mapN(_ :+ _)

  // By substituting these snippets back into the definition of `traverse` we can generalise it to work with any
  // Applicative:

  def listTraverse[F[_] : Applicative, A, B](list: List[A])(func: A => F[B]): F[List[B]] =
    list.foldLeft(List.empty[B].pure[F]) { (accum, item) =>
      (accum, func(item)).mapN(_ :+ _)
    }

  def listSequence[F[_]: Applicative, B](list: List[F[B]]): F[List[B]] =
    listTraverse(list)(identity)

  // We can use `listTraverse` to re-implement our uptime example:

  val totalUptime = listTraverse(hostnames)(getUptime)

  Await.result(totalUptime, 1.second) // List(1020, 960, 840)
}

object ExerciseTraversingWithVectors {

  //

}

object ExerciseTraversingWithOptions {

  //

}

object ExerciseTraversingWithValidated {

  //

}

object TraverseInCats {

}
