package com.github.jacobbishopxy.caseStudies.dataValidation

/**
 * Created by Administrator on 2/19/2020
 *
 * Transforming Data
 *
 * One of our requirements is the ability to transform data. This allows us to support additional scenarios like
 * parsing input. In this section we'll extend our check library with this additional functionality.
 *
 * The obvious starting point is `map`. When we try to implement this, we immediately run into a wall. Our current
 * definition of `Check` requires the input and output types to be the same:
 *
 * `type Check[E, A] = A => Either[E, A]`
 *
 * When we map over a check, what type do we assign to the result? It can't be `A` nor `B`. We are at an impasse:
 *
 * `def map(check: Check[E, A])(func: A => B): Check[E, ???]`
 *
 * To implement map we need to change the definition of `Check`. Specifically, we need to a new type variable to
 * separate the input type from the output:
 *
 * `type Check[E, A, B] = A => Either[E, B]`
 *
 * Checks can now represent operations like parsing a String as an Int:
 *
 * `val parseInt = Check[List[String], String, Int] = ???`
 *
 * However, splitting our input and output types raises another issue. Up until now we have operated under the
 * assumption that a Check always returns its input when successful. We used this in `and` and `or` to ignore the
 * output of the left and right rules and simply return the original input on success:
 *
 * `
 * (this(a), that(a)) match {
 * case And(left, right) => (left(a), right(a)).mapN((result1, result2) => Right(a))
 * // etc...
 * }
 * `
 *
 * In our new formulation we can't return `Right(a)` because its type is `Either[E, A]` not `Either[E, B]`. We're
 * forced to make an arbitrary choice between returning `Right(result1)` and `Right(result2)`. The same is true of
 * the `or` method. From this we can derive two things:
 *
 * - we should strive to make the laws we adhere to explicit;
 * - the code is telling us we have the wrong abstraction in `Check`.
 */
object TransformingData {

  /**
   * 1. Predicates
   *
   * We can make progress by pulling apart the concept of a `predicate`, which can be combined using logical operations
   * such as `and` and `or`, and the concept of a `check`, which can transform data.
   *
   * What we have called `Check` so far we will call `Predicate`. For `Predicate` we can state the following `identity`
   * law encoding the notion that a predicate always returns its input if it succeeds:
   *
   * For a predicate `p` of type `Predicate[E, A]` and elements `a1` and `a2` of type A, if `p(a1) == Success(a2)`
   * then `a1 == a2`.
   *
   * Making this change gives us the following code:
   */

  import cats.Semigroup
  import cats.data.Validated
  import cats.syntax.semigroup._ // for |+|
  import cats.syntax.apply._ // for mapN
  import cats.data.Validated._ // for Valid and Invalid

  sealed trait Predicate[E, A] {
    def and(that: Predicate[E, A]): Predicate[E, A] =
      And(this, that)

    def or(that: Predicate[E, A]): Predicate[E, A] =
      Or(this, that)

    def apply(a: A)(implicit s: Semigroup[E]): Validated[E, A] =
      this match {
        case Pure(func) => func(a)
        case And(left, right) =>
          (left(a), right(a)).mapN((_, _) => a)
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
  }

  final case class And[E, A](left: Predicate[E, A], right: Predicate[E, A]) extends Predicate[E, A]
  final case class Or[E, A](left: Predicate[E, A], right: Predicate[E, A]) extends Predicate[E, A]
  final case class Pure[E, A](func: A => Validated[E, A]) extends Predicate[E, A]

}

object TransformingDataP2 {

  /**
   * 2. Checks
   *
   * We'll use `Check` to represent a structure we build from a `Predicate` that also allows transformation of its
   * input. Implement `Check` with the following interface:
   */

  import cats.Semigroup
  import cats.data.Validated

  import TransformingData.Predicate

  sealed trait Check[E, A, B] {
    def apply(in: A)(implicit s: Semigroup[E]): Validated[E, B]

    def map[C](f: B => C): Check[E, A, C] = Map[E, A, B, C](this, f)
  }

  object Check {
    def apply[E, A](pred: Predicate[E, A]): Check[E, A, A] = Pure(pred)
  }

  final case class Map[E, A, B, C](check: Check[E, A, B], func: B => C)
    extends Check[E, A, C] {
    override def apply(in: A)(implicit s: Semigroup[E]): Validated[E, C] =
      check(in).map(func)
  }

  final case class Pure[E, A](pred: Predicate[E, A]) extends Check[E, A, A] {
    override def apply(in: A)(implicit s: Semigroup[E]): Validated[E, A] =
      pred(in)
  }

}

object TransformingDataP3 {
  /**
   * What about `flatMap`? The semantics are a bit unclear here. The method is simple enough to declare but it's not
   * so obvious what it means or how we should implement `apply`.
   *
   * How do we relate F in the figure to `Check` in our code? `Check` has three type variables while `F` only has one.
   *
   * To unify the types we need to fix two of the type parameters. The idiomatic choices are the error type `E` and
   * the input type `A`. In other words, the semantics of applying a `FlatMap` are:
   *
   * - given an input of type `A`, convert to `F[B]`;
   * - use the output of type `B` to choose a `Check[E, A, C]`;
   * - return to the original input of type `A` and apply it to the chosen check to generate the final result of
   * type `F[C]`.
   *
   * This is quite an odd method. We can implement it, but it is hard to find a use for it.
   */

  /**
   * It's the same implementation strategy as before with one wrinkle: `Validated` doesn't have a `flatMap` method.
   * To implement `flatMap` we must momentarily switch to `Either` and then switch back to `Validated`. The
   * `withEither` method on `Validated` does exactly this. From here we can just follow the types to implement `apply`.
   */

  import cats.Semigroup
  import cats.data.Validated

  import TransformingData.Predicate

  sealed trait Check[E, A, B] {
    def apply(in: A)(implicit s: Semigroup[E]): Validated[E, B]

    def map[C](f: B => C): Check[E, A, C] = Map[E, A, B, C](this, f)

    def flatMap[C](f: B => Check[E, A, C]): FlatMap[E, A, B, C] = FlatMap[E, A, B, C](this, f)
  }

  object Check {
    def apply[E, A](pred: Predicate[E, A]): Check[E, A, A] = Pure(pred)
  }

  final case class Map[E, A, B, C](check: Check[E, A, B], func: B => C)
    extends Check[E, A, C] {
    override def apply(in: A)(implicit s: Semigroup[E]): Validated[E, C] =
      check(in).map(func)
  }

  final case class Pure[E, A](pred: Predicate[E, A]) extends Check[E, A, A] {
    override def apply(in: A)(implicit s: Semigroup[E]): Validated[E, A] =
      pred(in)
  }

  final case class FlatMap[E, A, B, C](check: Check[E, A, B],
                                       func: B => Check[E, A, C]) extends Check[E, A, C] {
    def apply(a: A)(implicit s: Semigroup[E]): Validated[E, C] =
      check(a).withEither(_.flatMap(b => func(b)(a).toEither))
  }

}

object TransformingDataP4 {

  /**
   * We can write a more useful combinator that chains together two `Check`s. The output of the first check is
   * connected to the input of the second. This is analogous to function composition using `andThen`:
   *
   * `
   * val f: A => B = ???
   * val g: B => C = ???
   * val h: A => C = f andThen g
   * `
   *
   * A `Check` is basically a function `A => Validated[E, B]` so we can define an analogous `andThen` method:
   *
   * `
   * trait Check[E, A, B] {
   * def andThen[C](that: Check[E, B, C]): Check[E, A, C]
   * }
   * `
   */

  import cats.Semigroup
  import cats.data.Validated

  import TransformingData.Predicate

  sealed trait Check[E, A, B] {
    def apply(in: A)(implicit s: Semigroup[E]): Validated[E, B]

    def map[C](f: B => C): Check[E, A, C] = Map[E, A, B, C](this, f)

    def flatMap[C](f: B => Check[E, A, C]): FlatMap[E, A, B, C] = FlatMap[E, A, B, C](this, f)

    def andThen[C](that: Check[E, B, C]): Check[E, A, C] =
      AndThen[E, A, B, C](this, that)
  }

  object Check {
    def apply[E, A](pred: Predicate[E, A]): Check[E, A, A] = Pure(pred)
  }

  final case class Map[E, A, B, C](check: Check[E, A, B], func: B => C)
    extends Check[E, A, C] {
    override def apply(in: A)(implicit s: Semigroup[E]): Validated[E, C] =
      check(in).map(func)
  }

  final case class Pure[E, A](pred: Predicate[E, A]) extends Check[E, A, A] {
    override def apply(in: A)(implicit s: Semigroup[E]): Validated[E, A] =
      pred(in)
  }

  final case class FlatMap[E, A, B, C](check: Check[E, A, B],
                                       func: B => Check[E, A, C]) extends Check[E, A, C] {
    def apply(a: A)(implicit s: Semigroup[E]): Validated[E, C] =
      check(a).withEither(_.flatMap(b => func(b)(a).toEither))
  }

  final case class AndThen[E, A, B, C](check1: Check[E, A, B], check2: Check[E, B, C])
    extends Check[E, A, C] {
    override def apply(a: A)(implicit s: Semigroup[E]): Validated[E, C] =
      check1(a).withEither(_.flatMap(b => check2(b).toEither))
  }

}

