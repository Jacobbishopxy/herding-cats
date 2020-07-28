package com.github.jacobbishopxy.caseStudies.dataValidation

/**
 * Created by Jacob on 2/19/2020
 */
object Kleislis {

  /**
   * We'll finish off this case study by cleaning up the implementation of Check. A justifiable criticism of our
   * approach is that we've written a lot of code to do very little. A `Predicate` is essentially a function
   * `A => Validated[E, A]`, and a `Check` is basically a wrapper that lets us compose these functions.
   *
   * We can abstract `A => Validated[E, A]` to `A => F[B]`, which you'll recognise as the type of function you pass
   * to the `flatMap` method on a monad. Imagine we have the following sequence of operations:
   *
   * - We lift some value into a monad (by using `pure`, for example). This is a function with type `A => F[A]`.
   * - We then sequence some transformations on the monad using `flatMap`.
   *
   * We can write out this example using the monad API as follows:
   *
   * `
   * val aToB: A => F[B] = ???
   * val bToC: B => F[C] = ???
   *
   * def example[A, C](a: A): F[C] = aToB(a).flatMap(bToC)
   * `
   *
   * Recall that `Check` is, in the abstract, allowing us to compose functions of type `A => F[B]`. We can write the
   * above in terms of `andThen` as:
   *
   * `val aToC = aToB andThen bToC`
   *
   * The result is a (wrapped) function `aToC` of type `A => F[C]` that we can subsequently apply to a value of
   * type `A`.
   *
   * We have achieved the same thing as the `example` method without having to reference an argument of type `A`.
   * The `andThen` method on `Check` is analogous to function composition, but is composing function `A => F[B]`
   * instead of `A => B`.
   *
   * The abstract concept of composing functions of type `A => F[B]` has a name: a `Kleisli`.
   *
   * Cats contains a data type `cats.data.Kleisli` that wraps a function just as `Check` does. `Kleisli` has all the
   * methods of `Check` plus some additional ones. You've seen through its disguise and recognised it as another
   * concept from earlier in the book: `Kleisli` is just another name for `ReaderT`.
   *
   * Here is a simple example using `Kleisli` to transform an integer into a list of integers through three steps:
   */

  import cats.data.Kleisli
  import cats.instances.list._ // for Monad

  // These steps each transform an input `Int` into an output of type `List[Int]`:

  val step1: Kleisli[List, Int, Int] = Kleisli(x => List(x + 1, x - 1))

  val step2: Kleisli[List, Int, Int] = Kleisli(x => List(x, -x))

  val step3: Kleisli[List, Int, Int] = Kleisli(x => List(x * 2, x / 2))

  // We can combine the steps into a single pipeline that combines the underlying `List`s using `flatMap`:

  val pipeline: Kleisli[List, Int, Int] = step1 andThen step2 andThen step3

  /**
   * The result is a function that consumes a single `Int` and returns eight outputs, each produced by a different
   * combination of transformations from `step1`, `step2`, and `step3`:
   */

  pipeline.run(20) // List[Int] = List(42, 10, -42, -10, 38, 9, -38, -9)
}

object KleislisP2 {

  /**
   * The only notable difference between `Kleisli` and `Check` in terms of API is that `Kleisli` renames our apply
   * method to run.
   *
   * Let's replace `Check` with `Kleisli` in our validation examples. To do so we need to make a few changes to
   * `Predicate`. We must be able to convert a `Predicate` to a function, as `Kleisli` only works with functions.
   * Somewhat more subtly, when we convert a `Predicate` to a function, it should have type `A => Either[E, A]`
   * rather than `A => Validated[E, A]` because `Kleisli` relies on the wrapped function returning a monad.
   *
   * Add a method to `Predicate` called run that returns a function of the correct type. Leave the rest of the
   * code in `Predicate` the same.
   */

  import cats.Semigroup
  import cats.data.Validated
  import cats.data.Validated._
  import cats.syntax.apply._ // for mapN
  import cats.syntax.semigroup._ // for |+|
  import cats.syntax.validated._ // for Valid and Invalid

  sealed trait Predicate[E, A] {

    import Predicate._

    def and(that: Predicate[E, A]): Predicate[E, A] =
      And(this, that)

    def or(that: Predicate[E, A]): Predicate[E, A] =
      Or(this, that)

    def apply(a: A)(implicit s: Semigroup[E]): Validated[E, A] =
      this match {
        case Pure(func) => func(a)
        case And(left, right) => (left(a), right(a)).mapN((_, _) => a)
        case Or(left, right) =>
          left(a) match {
            case Valid(_) => Valid(a)
            case Invalid(e1) =>
              right(a) match {
                case Valid(_) => Valid(a)
                case Invalid(e2) => Invalid(e1 |+| e2)
              }
          }
      }

    def run(implicit s: Semigroup[E]): A => Either[E, A] =
      (a: A) => this (a).toEither
  }

  object Predicate {
    final case class And[E, A](left: Predicate[E, A],
                               right: Predicate[E, A]) extends Predicate[E, A]

    final case class Or[E, A](left: Predicate[E, A],
                              right: Predicate[E, A]) extends Predicate[E, A]

    final case class Pure[E, A](func: A => Validated[E, A]) extends Predicate[E, A]

    def apply[E, A](f: A => Validated[E, A]): Predicate[E, A] = Pure(f)

    def lift[E, A](err: E, fn: A => Boolean): Predicate[E, A] =
      Pure(a => if (fn(a)) a.valid else err.invalid)
  }

  /**
   * Now rewrite our username and email validation example in terms of `Kleisli` and `Predicate`. Here are few tips
   * in case you get stuck:
   *
   * First, remember that the `run` method on `Predicate` takes an implicit parameter. If you call `aPredicate.run(a)`
   * it will try to pass the implicit parameter explicitly. If you want to create a function from a `Predicate`
   * and immediately apply that function, use `aPredicate.run.apply(a)`.
   *
   * Second, type inference can be tricky in this exercise. We found that the following definitions helped us to
   * write code with fewer type declarations.
   */

  import cats.data.{Kleisli, NonEmptyList, Validated}
  import cats.instances.either._ // for Semigroupal
  import cats.instances.list._ // for Monad

  type Errors = NonEmptyList[String]

  def error(s: String): NonEmptyList[String] =
    NonEmptyList(s, Nil)

  type Result[A] = Either[Errors, A]

  type Check[A, B] = Kleisli[Result, A, B]

  // Create a check from a function:
  def check[A, B](func: A => Result[B]): Check[A, B] =
    Kleisli(func)

  // Create a check from a Predicate:
  def checkPred[A](pred: Predicate[Errors, A]): Check[A, A] =
    Kleisli[Result, A, A](pred.run)


  // Our base predicate definitions are essentially unchanged:

  def longerThan(n: Int): Predicate[Errors, String] =
    Predicate.lift(
      error(s"Must be longer than $n characters"),
      str => str.length > n)
  val alphanumeric: Predicate[Errors, String] =
    Predicate.lift(
      error(s"Must be all alphanumeric characters"),
      str => str.forall(_.isLetterOrDigit))
  def contains(char: Char): Predicate[Errors, String] =
    Predicate.lift(
      error(s"Must contain the character $char"),
      str => str.contains(char))
  def containsOnce(char: Char): Predicate[Errors, String] =
    Predicate.lift(
      error(s"Must contain the character $char only once"),
      str => str.filter(c => c == char).length == 1)

  // Our username and email examples are slightly different in that we make use of `check()` and `checkPred()` in
  // different situations:

  val checkUsername: Check[String, String] =
    checkPred(longerThan(3) and alphanumeric)

  val splitEmail: Check[String, (String, String)] =
    check(_.split('@') match {
      case Array(name, domain) => Right((name, domain))
      case _ => Left(error("Must contain a single @ character"))
    })

  val checkLeft: Check[String, String] =
    checkPred(longerThan(0))

  val checkRight: Check[String, String] =
    checkPred(longerThan(3) and contains('.'))

  val joinEmail: Check[(String, String), String] =
    check { case (l, r) => (checkLeft(l), checkRight(r)).mapN(_ + "@" + _) }

  val checkEmail: Check[String, String] =
    splitEmail andThen joinEmail

  // Finally, we can see that our `createUser` example works as expected using Kleisli:

  final case class User(username: String, email: String)

  def createUser(username: String, email: String): Either[Errors, User] =
    (checkUsername.run(username), checkEmail.run(email)).mapN(User)

  createUser("Jacob", "jacob@example.com")

  createUser("", "sam@example@com")

}

































































