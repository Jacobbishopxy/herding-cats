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

  def listSequence[F[_] : Applicative, B](list: List[F[B]]): F[List[B]] =
    listTraverse(list)(identity)

  // We can use `listTraverse` to re-implement our uptime example:

  val totalUptime: Future[List[Int]] = listTraverse(hostnames)(getUptime)

  Await.result(totalUptime, 1.second) // List(1020, 960, 840)
}

object ExerciseTraversingWithVectors {

  // What is the result of the following?

  import TraverseTypeClass.listSequence
  import cats.instances.vector._ // for Applicative

  listSequence(List(Vector(1, 2), Vector(3, 4)))
  // Vector(List(1,3),List(1,4),List(2,3),List(2,4))

  // What about a list of three parameters?

  listSequence(List(Vector(1, 2), Vector(3, 4), Vector(5, 6)))
  // Vector(List(1,3,5),List(1,3,6),List(1,4,5),List(1,4,6),List(2,3,5),List(2,3,6),List(2,4,5),List(2,4,6))

  /**
   * Note:
   *
   * Since input type is `List[Vector[Int]]`, `F` is `Vector` which uses `Applicative[Vector].product` as combinator.
   * However, `Vector` is `Monad`, and `Monad`'s `product` is based on `flatMap` method. Therefore, it turns out to
   * calculate cartesian product for each element in `Vector`.
   */
}

object ExerciseTraversingWithOptions {

  // Here's an example that uses Options:

  import TraverseTypeClass.listTraverse
  import cats.instances.option._ // for Applicative


  def process(inputs: List[Int]): Option[List[Int]] =
    listTraverse(inputs)(n => if (n % 2 == 0) Some(n) else None)

  // What is the return type of this method? What does it produce for the following inputs?

  process(List(2, 4, 6))
  // Some(List(2, 4, 6))

  process(List(1, 2, 3))
  // None

}

object ExerciseTraversingWithValidated {

  // Finally, here is an example that uses `Validated`:

  import TraverseTypeClass.listTraverse
  import cats.data.Validated
  import cats.instances.list._ // for Monoid

  type ErrorsOr[A] = Validated[List[String], A]

  def process(inputs: List[Int]): ErrorsOr[List[Int]] =
    listTraverse(inputs) { n =>
      if (n % 2 == 0) Validated.valid(n)
      else Validated.invalid(List(s"$n is not even"))
    }

  // What does this method produce for the following inputs?

  process(List(2, 4, 6))
  // Valid(List(2, 4, 6))
  process(List(1, 2, 3))
  // Invalid(List("1 is not even", "3 is not even"))

  /**
   * Note:
   *
   * The return type here is `ErrorOr[List[Int]]`, which expands to `Validated[List[String], List[Int]]`.
   * The semantics for semigroupal combine on validated are accumulating error handling, so the result is
   * either a list of even Ints, or a list of errors detailing which Ints failed the test.
   */
}

object TraverseInCats {

  /**
   * Our `listTraverse` and `listSequence` methods work with any type of `Applicative`, but they only work with
   * one type of sequence: `List`. We can generalise over different sequence types using a type class, which brings
   * us to Cats' `Traverse`. Here's the abbreviated definition:
   *
   * `
   * package cats
   * trait Traverse[F[_]] {
   * def traverse[G[_]: Applicative, A, B]
   * (inputs: F[A])(func: A => G[B]): G[F[B]]
   * def sequence[G[_]: Applicative, B]
   * (inputs: F[G[B]]): G[F[B]] =
   * traverse(inputs)(identity)
   * }
   * `
   *
   * Cats provides instances of `Traverse` for `List`, `Vector`, `Stream`, `Option`, `Either`, and a variety of other
   * types. We can summon instances as usual using `Traverse.apply` and use the `traverse` and sequence methods as
   * described in the previous section:
   */

  import TraverseTypeClass.{hostnames, getUptime}

  import cats.Traverse
  import cats.instances.future._ // for Applicative
  import cats.instances.list._ // for Traverse

  import scala.concurrent.{Future, Await}
  import scala.concurrent.duration._
  import scala.concurrent.ExecutionContext.Implicits.global


  val totalUptime: Future[List[Int]] = Traverse[List].traverse(hostnames)(getUptime)

  Await.result(totalUptime, 1.second)
  // List(1020, 960, 840)

  val numbers: List[Future[Int]] = List(Future(1), Future(2), Future(3))

  val numbers2: Future[List[Int]] = Traverse[List].sequence(numbers)

  Await.result(numbers2, 1.second)
  // List(1, 2, 3)

  /**
   * There are also syntax versions of the methods, imported via `cats.syntax.traverse`:
   */

  import cats.syntax.traverse._ // for sequence and traverse

  Await.result(hostnames.traverse(getUptime), 1.second)
  Await.result(numbers.sequence, 1.second)

  /**
   * As you can see, this is much more compact and readable than the `foldLeft` code we started with earlier
   * this chapter!
   */
}
