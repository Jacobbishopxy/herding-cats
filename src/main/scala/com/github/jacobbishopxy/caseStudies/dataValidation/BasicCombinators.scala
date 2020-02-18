package com.github.jacobbishopxy.caseStudies.dataValidation

/**
 * Created by Administrator on 2/18/2020
 */
object BasicCombinators {

  /**
   * Let's add some combinator methods to Check, starting with `and`. This method combines two checks into one,
   * succeeding only if both checks succeed. Think about implementing this method now.
   *
   * The problem is: what do you do when `both` checks fail? The correct thing to do is to return both errors,
   * but we don't currently have any way to combine `E`s. We need a type class that abstracts over the concept of
   * "accumulating" errors.
   */

  //  trait Check[E, A] {
  //    def and(that: Check[E, A]): Check[E, A] = ???
  //  }

  /**
   * There is another semantic issue that will come up quite quickly: should and short-circuit if the first check
   * fails.
   */

  import cats.Semigroup
  import cats.syntax.either._ // for asLeft and asRight
  import cats.syntax.semigroup._ // for |+|

  final case class CheckF[E, A](func: A => Either[E, A]) {
    def apply(a: A): Either[E, A] = func(a)

    def and(that: CheckF[E, A])(implicit s: Semigroup[E]): CheckF[E, A] =
      CheckF { a =>
        (this (a), that(a)) match {
          case (Left(e1), Left(e2)) => (e1 |+| e2).asLeft
          case (Left(e), Right(_)) => e.asLeft
          case (Right(_), Left(e)) => e.asLeft
          case (Right(_), Right(_)) => a.asRight
        }
      }
  }

  // test

  import cats.instances.list._ // for Semigroup

  val a: CheckF[List[String], Int] = CheckF { v =>
    if (v > 2) v.asRight else List("Must be > 2").asLeft
  }

  val b: CheckF[List[String], Int] = CheckF { v =>
    if (v < -2) v.asRight else List("Must be < -2").asLeft
  }

  val check: CheckF[List[String], Int] = a and b

  val c1: Either[List[String], Int] = check(5) // Left(List(Must be < -2))
  val c2: Either[List[String], Int] = check(0) // Left(List(Must be > 2, Must be < -2))

  /**
   * What happens if we try to create checks that fail with a type that we can't accumulate? For example, there is no
   * `Semigroup` instance for `Nothing`. Now let's see another implementation strategy. In this approach we model
   * checks as an algebraic data type, with an explicit data type for each combinator. We call this implementation
   * `Check`:
   */

  sealed trait Check[E, A] {
    def and(that: Check[E, A]): Check[E, A] =
      And(this, that)

    def apply(a: A)(implicit s: Semigroup[E]): Either[E, A] =
      this match {
        case Pure(func) => func(a)

        case And(left, right) =>
          (left(a), right(a)) match {
            case (Left(e1), Left(e2)) => (e1 |+| e2).asLeft
            case (Left(e), Right(_)) => e.asLeft
            case (Right(_), Left(e)) => e.asLeft
            case (Right(_), Right(_)) => a.asRight
          }
      }
  }

  final case class And[E, A](left: Check[E, A], right: Check[E, A]) extends Check[E, A]
  final case class Pure[E, A](func: A => Either[E, A]) extends Check[E, A]

  // test

  val a1: Check[List[String], Int] = Pure { v =>
    if (v > 2) v.asRight else List("Must be > 2").asLeft
  }

  val a2: Check[List[String], Int] = Pure { v =>
    if (v < -2) v.asRight else List("Must be < -2").asLeft
  }

  val check2: Check[List[String], Int] = a1 and a2

  /**
   * While the ADT implementation is more verbose than the function wrapper implementation, it has the advantage of
   * cleanly separating the structure of the computation (the ADT instance we create) from the process that gives it
   * meaning (the apply method). From here we have a number of options:
   *
   * - inspect and refactor checks after they are created;
   * - move the apply "interpreter" out into its own module;
   * - implement alternative interpreters providing other functionality.
   *
   * Because of its flexibility, we will use the ADT implementation for the rest of this case study.
   */

}

object BasicCombinators2 {

  /**
   * Strictly speaking, `Either[E, A]` is the wrong abstraction for the output of our check.
   *
   * The implementation of `apply` for `And` is using the pattern for applicative functors. `Either` has an
   * `Applicative` instance, but it doesn't have the semantics we want. It fails fast instead of accumulating errors.
   *
   * If we want to accumulate errors `Validated` is a more appropriate abstraction. As a bonus, we get more code
   * reuse because we can lean on the applicative instance of `Validated` in the implementation of `apply`.
   *
   * Here's the complete implementation:
   */

  import cats.Semigroup
  import cats.data.Validated
  import cats.syntax.apply._ // for mapN

  sealed trait Check[E, A] {
    def and(that: Check[E, A]): Check[E, A] =
      And(this, that)

    def apply(a: A)(implicit s: Semigroup[E]): Validated[E, A] =
      this match {
        case Pure(func) => func(a)
        case And(left, right) => (left(a), right(a)).mapN((_, _) => a)
      }
  }

  final case class And[E, A](left: Check[E, A], right: Check[E, A]) extends Check[E, A]
  final case class Pure[E, A](func: A => Validated[E, A]) extends Check[E, A]
}

object BasicCombinators3 {

  /**
   * Our implementation is looking pretty good now. Implement an `or` combinator to complement and.
   */

  import cats.Semigroup
  import cats.data.Validated
  import cats.syntax.semigroup._ // for |+|
  import cats.syntax.apply._ // for mapN

  sealed trait Check[E, A] {
    def and(that: Check[E, A]): Check[E, A] =
      And(this, that)

    def or(that: Check[E, A]): Check[E, A] =
      Or(this, that)

    def apply(a: A)(implicit s: Semigroup[E]): Validated[E, A] =
      this match {
        case Pure(func) => func(a)
        case And(left, right) => (left(a), right(a)).mapN((_, _) => a)
        case Or(left, right) =>
          left(a) match {
            case Validated.Valid(a) => Validated.Valid(a)
            case Validated.Invalid(e1) => right(a) match {
              case Validated.Valid(a) => Validated.Valid(a)
              case Validated.Invalid(e2) => Validated.Invalid(e1 |+| e2)
            }
          }
      }
  }

  final case class And[E, A](left: Check[E, A], right: Check[E, A]) extends Check[E, A]
  final case class Or[E, A](left: Check[E, A], right: Check[E, A]) extends Check[E, A]
  final case class Pure[E, A](func: A => Validated[E, A]) extends Check[E, A]

  /**
   * With `and` and `or` we can implement many of checks we'll want in practice. However, we still have a few more
   * methods to add. We'll turn to `map` and related methods next.
   */

}

