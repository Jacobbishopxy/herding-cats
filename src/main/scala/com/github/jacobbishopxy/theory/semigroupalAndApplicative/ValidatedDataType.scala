package com.github.jacobbishopxy.theory.semigroupalAndApplicative

/**
 * Created by jacob on 2/7/2020
 *
 * By now we are familiar with the fail-fast error handling behaviour of Either. Furthermore, because
 * Either is a monad, we know that the semantics of product are the same as those for `flatMap`. In fact,
 * it is impossible for us to design monadic data type that implements error accumulating semantics
 * without breaking the consistency of these two methods.
 */
object ValidatedDataType {

  /**
   * Fortunately, Cats provides a data type called `Validated` that has an instance of `Semigroupal` but
   * no instance of Monad. The implementation of `product` is therefore free to accumulate errors:
   */

  import cats.Semigroupal
  import cats.data.Validated
  import cats.instances.list._ // for Monoid

  type AllErrorsOr[A] = Validated[List[String], A]

  Semigroupal[AllErrorsOr].product(
    Validated.invalid(List("Error 1")),
    Validated.invalid(List("Error 2"))
  )
  //  AllErrorsOr[(Nothing, Nothing)] = Invalid(List(Error 1, Error 2))

  /**
   * `Validated` complements Either nicely. Between the two we have support for both of the common types
   * of error handling: fail-fast and accumulating.
   */

  /**
   * 1. Creating Instances of Validated
   *
   * `Validated` has two subtypes, `Validated.Valid` and `Validated.Invalid`, that correspond loosely to
   * `Right` and `Left`. There are a lot of ways to create instances of these types. We can create them
   * directly using their `apply` methods:
   */

  val v = Validated.Valid(123)
  // v: cats.data.Validated.Valid[Int] = Valid(123)
  val i = Validated.invalid(List("Badness"))
  // i: cats.data.Validated.Invalid[List[String]] = Invalid(List(Badness))

  // As a third option we can import the `valid` and `invalid` extension methods from `cats.syntax.validated`:
  import cats.syntax.validated._ // for valid and invalid

  123.valid[List[String]]
  // cats.data.Validated[List[String],Int] = Valid(123)
  List("Badness").invalid[Int]
  // cats.data.Validated[List[String],Int] = Invalid(List(Badness))

  /**
   * As a fourth option we can use `pure` and `raiseError` from `cats.syntax.applicative` and
   * `cats.syntax.applicativeError` respectively:
   */

  import cats.syntax.applicative._ // for pure
  import cats.syntax.applicativeError._ // for raiseError

  type ErrorsOr[A] = Validated[List[String], A]

  123.pure[ErrorsOr]
  // ErrorsOr[Int] = Valid(123)
  List("Badness").raiseError[ErrorsOr, Int]
  // ErrorsOr[Int] = Invalid(List(Badness))

  /**
   * Finally, there are helper methods to create instances of `Validated` from different sources. We can
   * create them from `Exceptions`, as well as instances of `Try`, `Either`, and `Option`:
   */

  Validated.catchOnly[NumberFormatException]("foo".toInt)
  // cats.data.Validated[NumberFormatException,Int] =
  // Invalid(java.lang.NumberFormatException: For input string: "foo")
  Validated.catchNonFatal(sys.error("Badness"))
  // cats.data.Validated[Throwable,Nothing] =
  // Invalid(java.lang.RuntimeException: Badness)
  Validated.fromTry(scala.util.Try("foo".toInt))
  // cats.data.Validated[Throwable,Int] =
  // Invalid(java.lang.NumberFormatException: For input string: "foo")
  Validated.fromEither[String, Int](Left("Badness"))
  // cats.data.Validated[String,Int] = Invalid(Badness)
  Validated.fromOption[String, Int](None, "Badness")
  // cats.data.Validated[String,Int] = Invalid(Badness)

  /**
   * 2. Combining Instances of Validated
   *
   * We can combine instances of `Validated` using any of the methods or syntax described for `Semigroupal`
   * above.
   *
   * All of these techniques require an instance of `Semigroupal` to be in scope. As with `Either`, we need
   * to fix the error type to create a type constructor with the correct number of parameters for
   * `Semigroupal`:
   */

  type AllErrorsOr2[A] = Validated[String, A]

  /**
   * `Validated` accumulates error using a `Semigroup`, so we need one of those in scope to summon the
   * `Semigroupal`. If no `Semigroup` is visible at the call site, we get an annoyingly unhelpful compilation
   * error.
   *
   * Once we import a `Semigroup` for the error type, everything works as expected:
   */

  import cats.instances.string._ // for Semigroup

  Semigroupal[AllErrorsOr2]

  /**
   * As long as the compiler has all the implicits in scope to summon a `Semigroupal` of the correct type,
   * we can use apply syntax or any of the other `Semigroupal` methods to accumulate errors as we like:
   */

  import cats.syntax.apply._ // for tupled

  (
    "Error 1".invalid[Int],
    "Error 2".invalid[Int]
  ).tupled
  // cats.data.Validated[String,(Int, Int)] = Invalid(Error 1 Error 2)

  /**
   * As you can see, `String` isn't an ideal type for accumulating errors. We commonly use `List`s or
   * `Vector`s instead:
   */

  import cats.instances.vector._ // for Semigroupal

  (
    Vector(404).invalid[Int],
    Vector(500).invalid[Int]
  ).tupled
  // cats.data.Validated[scala.collection.immutable.Vector[Int],(Int, Int)] = Invalid(Vector(404, 500))

  /**
   * The `cats.data` package also provides the `NonEmptyList` and `NonEmptyVector` types that prevent us
   * failing without at least one error:
   */

  import cats.data.NonEmptyVector

  (
    NonEmptyVector.of("Error 1").invalid[Int],
    NonEmptyVector.of("Error 2").invalid[Int]
  ).tupled
  // cats.data.Validated[cats.data.NonEmptyVector[String],(Int,Int)] =
  // Invalid(NonEmptyVector(Error 1, Error 2))

  /**
   * 3. Methods of Validated
   *
   * `Validated` comes with a suite of methods that closely resemble those available for `Either`, including
   * the methods from `cats.syntax.either`. We can use `map`, `leftMap`, and `bimap` to transform the values
   * inside the valid and invalid sides:
   */

  123.valid.map(_ * 100)
  // cats.data.Validated[Nothing,Int] = Valid(12300)
  "?".invalid.leftMap(_.toString)
  // cats.data.Validated[String,Nothing] = Invalid(?)
  123.valid[String].bimap(_ + "!", _ * 100)
  // cats.data.Validated[String,Int] = Valid(12300)
  "?".invalid[Int].bimap(_ + "!", _ * 100)
  // cats.data.Validated[String,Int] = Invalid(?!)

  /**
   * We can't `flatMap` because `Validated` isn't a monad. However, we can convert back and forth between
   * `Validated` and `Either` using the `toEither` and `toValidated` methods. Note that `toValidated` comes
   * from `cats.syntax.either`:
   */

  import cats.syntax.either._ // for toValidated

  "Badness".invalid[Int]
  // cats.data.Validated[String,Int] = Invalid(Badness)
  "Badness".invalid[Int].toEither
  // Either[String,Int] = Left(Badness)
  "Badness".invalid[Int].toEither.toValidated
  // cats.data.Validated[String,Int] = Invalid(Badness)

  /**
   * We can even use the `withEither` method to temporarily convert to an `Either` and convert back again
   * immediately:
   */

  41.valid[String].withEither(_.flatMap(n => Right(n + 1)))
  // cats.data.Validated[String,Int] = Valid(42)

  /**
   * There is also a `withValidated` method in `vats.syntax.either`.
   *
   * As with Either, we can use the ensure method to fail with a specified error if a predicate does not
   * hold:
   */

  123.valid[String].ensure("Negative!")(_ > 0)

  /**
   * Finally, we can call `getOrElse` or `fold` to extract values from the `Valid` and `Invalid` cases:
   */

  "fail".invalid[Int].getOrElse(0)
  // Int = 0

  "fail".invalid[Int].fold(_ + "!!!", _.toString)
  // String = fail!!!
}

object ExerciseFormValidation {

  // Form Validation

}
