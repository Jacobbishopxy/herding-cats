package com.github.jacobbishopxy.theory.monads

/**
 * Created by jacob on 2/11/2020
 *
 * Cats provides an additional type class called `MonadError` that abstracts over `Either`-like data types
 * that are used for error handling. `MonadError` provides extra operations for raising and handling errors.
 *
 * You won't need to use `MonadError` unless you need to abstract over error handling monads. For example,
 * you can use `MonadError` to abstract over `Future` and `Try`, or over `Either` and `EitherT`.
 */
object ErrorHandlingAndMonadError {

  /**
   * 1. The MonadError Type Class
   *
   * Here is a simplified version of the definition of `MonadError`:
   */

  //  package cats
  //  trait MonadError[F[_], E] extends Monad[F] {
  //    // Lift an error into the `F` context:
  //    def raiseError[A](e: E): F[A]
  //    // Handle an error, potentially recovering from it:
  //    def handleError[A](fa: F[A])(f: E => A): F[A]
  //    // Test an instance of `F`,
  //    // failing if the predicate is not satisfied:
  //    def ensure[A](fa: F[A])(e: E)(f: A => Boolean): F[A]
  //  }

  /**
   * `MonadError` is defined in terms of two type parameters:
   *
   * - `F` is the type of the monad;
   * - `E` is the type of error contained within `F`.
   *
   * To demonstrate how these parameters fit together, here's an example where we instantiate the type class
   * for `Either`:
   */

  import cats.MonadError
  import cats.instances.either._ // for MonadError

  type ErrorOr[A] = Either[String, A]

  val monadError: MonadError[ErrorOr, String] = MonadError[ErrorOr, String]

  /**
   * ApplicativeError:
   * In reality, `MonadError` extends another type class called `ApplicativeError`.
   */

  /**
   * 2. Raising and Handling Errors
   *
   * The two most important methods of `MonadError` are `raiseError` and `handleError`. `raiseError` is like
   * the `pure` method for Monad except that it creates an instance representing a failure:
   */

  val success: ErrorOr[Int] = monadError.pure(42) // Right(42)
  val failure: ErrorOr[Nothing] = monadError.raiseError("Badness") // Left(Badness)

  /**
   * `handleError` is the compliment of `raiseError`. It allows us to consume an error and (possibly) turn it
   * into a success, similar to the `recover` method of `Future`:
   */

  monadError.handleError(failure) {
    case "Badness" => monadError.pure("It's ok")
    case _ => monadError.raiseError("It's not ok")
  } // ErrorOr[ErrorOr[String]] = Right(Right(It's ok))

  /**
   * There is also a third useful method called `ensure` that implements `filter`-like behaviour. We test the
   * value of a successful monad with a predicate and specify an error to raise if the predicate returns false:
   */

  import cats.syntax.either._ // for asRight

  monadError.ensure(success)("Number too low!")(_ > 1000)
  // ErrorOr[Int] = Left(Number too low!)

  /**
   * Cats provides syntax for `raiseError` and `handleError` via `cats.syntax.applicativeError` and ensure via
   * `cats.syntax.monadError`:
   */

  import cats.syntax.applicative._ // for pure
  import cats.syntax.applicativeError._ // for raiseError etc
  //import cats.syntax.monadError._ // for ensure

  val success2: ErrorOr[Int] = 42.pure[ErrorOr]
  // success2: ErrorOr[Int] = Right(42)
  val failure2: ErrorOr[Int] = "Badness".raiseError[ErrorOr, Int]
  // failure2: ErrorOr[Int] = Left(Badness)
  success2.ensure("Number to low!")(_ > 1000)
  // Either[String, Int] = Left(Number to low!)

  /**
   * 3. Instances of MonadError
   *
   * Cats provides instances of `MonadError` for numerous data types including `Either`, `Future`, and `Try`.
   * The instance for `Either` is customisable to any error type, whereas the instances for `Future` and `Try`
   * always represent errors as `Throwable`:
   */

  import scala.util.Try
  import cats.instances.try_._ // for MonadError

  val exn: Throwable = new RuntimeException("It's all gone wrong")

  exn.raiseError[Try, Int]
  // scala.util.Try[Int] = Failure(java.lang.RuntimeException: It's all gone wrong)
}
