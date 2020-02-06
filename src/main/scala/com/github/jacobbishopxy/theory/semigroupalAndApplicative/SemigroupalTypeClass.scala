package com.github.jacobbishopxy.theory.semigroupalAndApplicative

/**
 * Created by jacob on 2/6/2020
 *
 * In previous chapters we saw how functors and monads let us sequence operations using `map` and `flatMap`.
 * While functors and monads are both immensely useful abstractions, there are certain types of program
 * flow that they cannot represent.
 *
 * One such example is form validation. When we validate a form we want to return all the errors to the
 * user, not stop on the first error we encounter. If we model this with a monad like `Either`, we fail
 * fast and lose errors.
 *
 * Another example is the concurrent evaluation of Futures. If we have several long-running independent
 * tasks, it makes sense to execute them concurrently. However, monadic comprehension only allows us to
 * run them in sequence. `map` and `flatMap` aren't quite capable of capturing what we want because they
 * make the assumption that each computation is dependent on the previous one.
 *
 * The calls to parseInt and `Future.apply` above are independent of one another, but `map` and `flatMap`
 * can't exploit this. We need a weaker construct one that doesn't guarantee sequencing -- to achieve the
 * result we want. In this chapter we will look at two type classes that support this pattern:
 *
 * - `Semigroupal` encompasses the notion of composing pairs of contexts. Cats provides a `cats.syntax.apply`
 * module that makes use of `Semigroupal` and `Functor` to allow users to sequence functions with multiple
 * arguments.
 *
 * - `Applicative` extends `Semigroupal` and `Functor`. It provides a way of applying functions to
 * parameters within a context. `Applicative` is the source of the `pure` method we introduced in Chapter 4.
 *
 * Applicatives are often formulated in terms of function application, instead of the semigroupal
 * formulation that is emphasised in Cats. This alternative formulation provides a link to other libraries
 * and languages such as Scalaz and Haskell. We'll take a look at different formulations of Applicative,
 * as well as the relationships between `Semigroupal`, `Functor`, `Applicative`, and `Monad`, towards the
 * end of the chapter.
 */
object SemigroupalTypeClass {

  /**
   * `cats.semigroupal` is a type class that allows us to combine contexts. If we have two objects of type
   * `F[A]` and `F[B]`, a `Semigroupal[F]` allows us to combine them to form an `F[(A, B)]`. Its definition
   * in Cats is:
   *
   * `
   * trait Semigroupal[F[_]] {
   * def product[A, B](fa: F[A], fb: F[B]): F[(A, B)]
   * }
   * `
   *
   * As we discussed at the beginning of this chapter, the parameters `fa` and `fb` are independent of
   * one another: we can compute them in either order before passing them to product. This is in contrast
   * to `flatMap`, which imposes a strict order on its parameters. This gives us more freedom when defining
   * instances of `Semigroupal` than we get when defining Monads.
   */

  /**
   * 1. Joining Two Contexts
   *
   * While `Semigroup` allows us to join values, `Semigroupal` allows us to join contexts. Let's join some
   * Options as an example:
   */

  import cats.Semigroupal
  import cats.instances.option._ // for Semigroupal

  Semigroupal[Option].product(Some(123), Some("abc"))
  // Option[(Int, String)] = Some((123, abc))

  /**
   * If both parameters are instances of `Some`, we end up with a tuple of the values within. If either
   * parameter evaluates to `None`, the entire result is `None`:
   */

  Semigroupal[Option].product(None, Some("abc"))
  // Option[(Nothing, String)] = None
  Semigroupal[Option].product(Some(123), None)
  // Option[(Int, Nothing)] = None

  /**
   * 2. Joining Three or More Contexts
   *
   * The companion object for `Semigroupal` defines a set of methods on top of product. For example, the
   * methods `tuple2` through `tuple22` generalise product to different arities:
   */

  Semigroupal.tuple3(Option(1), Option(2), Option(3))
  // Option[(Int, Int, Int)] = Some((1, 2, 3))
  Semigroupal.tuple3(Option(1), Option(2), Option.empty[Int])
  // Option[(Int, Int, Int)] = None

  /**
   * The methods `map2` through `map22` apply a user-specified function to the value inside 2 to 22
   * contexts:
   */
  Semigroupal.map3(Option(1), Option(2), Option(3))(_ + _ + _)
  // Option[Int] = Some(6)
  Semigroupal.map2(Option(1), Option.empty[Int])(_ + _)
  // Option[Int] = None

  /**
   * There are also methods `contramap2` through `contramap22` and `imap2` through `imap22`, that require
   * instances of `Contravariant` and `Invariant` respectively.
   */
}
