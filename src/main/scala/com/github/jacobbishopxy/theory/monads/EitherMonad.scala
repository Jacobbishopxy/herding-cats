package com.github.jacobbishopxy.theory.monads

/**
 * Created by jacob on 2/3/2020
 *
 * Let's look at another useful monad: the `Either` type from the Scala standard library.
 * In Scala 2.12, `Either` become right biased.
 */
object EitherMonad {

  /**
   * 1. Left and Right Bias
   *
   * In Scala 2.12, Either was redesigned. The modern Either makes the decision that the right side
   * represents the success case and thus supports map and flatMap directly. This makes for
   * comprehensions much more pleasant:
   */

  val either1: Either[String, Int] = Right(10)
  val either2: Either[String, Int] = Right(32)

  for {
    a <- either1
    b <- either2
  } yield a + b // Right(42)

  /**
   * 2. Creating Instances
   *
   * In addition to creating instances of Left and Right directly, we can also import the asLeft and
   * asRight extension methods from `cats.syntax.either`:
   */

  import cats.syntax.either._ // for asRight

  val a = 3.asRight[String]
  val b = 4.asRight[String]

  for {
    x <- a
    y <- b
  } yield x * x + y * y // Right(25)

  /**
   * These "smart constructors" have advantages over `Left.apply` and `Right.apply` because they return
   * results of type `Either` instead of Left and Right. This helps avoid type inference bugs caused by
   * over-narrowing, like the bug in the example below:
   */

  //  def countPositive(nums: List[Int]) =
  //    nums.foldLeft(Right(0)) {(acc, num) =>
  //      if (num > 0) acc.map(_ + 1)
  //      else Left("Negative. Stopping!")
  //    }

  /**
   * This code fails to compile for two reasons:
   *
   * 1. the compiler infers the type of the accumulator as Right instead of Either;
   * 2. we didn't specify type parameters for `Right.apply` so the compiler infers the left parameter
   * as `Nothing`.
   *
   * Switching to `asRight` avoids both of these problems. `asRight` has a return type of `Either`, and
   * allows us to completely specify the type with only one type parameter:
   */

  def countPositive(nums: List[Int]): Either[String, Int] =
    nums.foldLeft(0.asRight[String]) { (acc, num) =>
      if (num > 0) acc.map(_ + 1)
      else Left("Negative. Stopping!")
    }

  countPositive(List(1, 2, 3)) // Right(3)
  countPositive(List(1, -1, 2)) // Left(Negative. Stopping!)

  /**
   * `cats.syntax.either` adds some useful extension methods to the Either companion object. The
   * `catchOnly` and `catchNonFatal` methods are great for capturing Exceptions as instances of
   * Either:
   */

  Either.catchOnly[NumberFormatException]("foo".toInt)
  // Left(java.lang.NumberFormatException: For input string: "foo")
  Either.catchNonFatal(sys.error("Badness"))
  // Left(java.lang.RuntimeException: Badness)

  // There are also methods for creating an Either from other data types:
  Either.fromTry(scala.util.Try("foo".toInt))
  // Left(java.lang.NumberFormatException: For input string: "foo")
  Either.fromOption[String, Int](None, "Badness")
  // Left(Badness)

  /**
   * 3. Transforming Eithers
   *
   * `cats.syntax.either` also adds some useful methods to instances of Either. We can use `orElse` and
   * `getOrElse` to extract values from the right side or return a default:
   */

  import cats.syntax.either._

  "Error".asLeft[Int].getOrElse(0) // 0
  "Error".asLeft[Int].orElse(2.asRight[String]) // Right(2)

  // The ensure method allows us to check whether the right-hand value satisfies a predicate:
  (-1).asRight[String].ensure("Must be non-negative!")(_ > 0)

  // The `recover` and `recoverWith` methods provide similar error handling to their namesakes on `Future`:
  "error".asLeft[Int].recover {
    case _: String => -1
  } // Right(-1)

  "error".asLeft[Int].recoverWith {
    case _: String => Right(-1)
  } // Right(-1)

  // There are `leftMap` and `bimap` methods to complement map:
  "foo".asLeft[Int].leftMap(_.reverse) // Left(oof)
  6.asRight[String].bimap(_.reverse, _ * 7) // Right(42)
  "bar".asLeft[Int].bimap(_.reverse, _ * 7) // Left(rab)

  // The swap method lets us exchange left for right:
  123.asRight[String] // Right(123)
  123.asRight[String].swap // Left(123)

  // Finally, Cats adds a host of conversion methods: `toOption`, `toList`, `toTry`, `toValidated`, and so on.

  /**
   * 4. Error Handling
   *
   * Either is typically used to implement fail-fast error handling. We sequence computations using `flatMap`
   * as usual. If one computation fails, the remaining computations are not run:
   */

  for {
    a <- 1.asRight[String]
    b <- 0.asRight[String]
    c <- if (b == 0) "DIV0".asLeft[Int] else (a / b).asRight[String]
  } yield c * 100 // Left(DIV0)

  // When using Either for error handling, we need to determine what type we want to use to represent errors.
  // We could use `Throwable` for this:
  type Result[A] = Either[Throwable, A]

  /**
   * This gives us similar semantics to `scala.util.Try`. The problem, however, is that `Throwable` is an
   * extremely broad type. We have (almost) no idea about what type of error occurred.
   *
   * Another approach is to define an algebraic data type to represent errors that may occur in our program:
   */
  sealed trait LoginError extends Product with Serializable

  final case class UserNotFound(username: String) extends LoginError
  final case class PasswordIncorrect(username: String) extends LoginError
  case object UnexpectedError extends LoginError

  case class User(username: String, password: String)

  type LoginResult = Either[LoginError, User]

  /**
   * This approach solves the problems we saw with `Throwable`. It gives us a fixed set of expected error type
   * and a catch-all for anything else that we didn't expect. We also get the safety of exhaustively checking
   * on any pattern matching we do:
   */

  def handleError(error: LoginError): Unit =
    error match {
      case UserNotFound(u) => println(s"User not found: $u")
      case PasswordIncorrect(u) => println(s"Password incorrect: $u")
      case UnexpectedError => println("Unexpected error")
    }

  val result1 = User("jacob", "qwe").asRight
  val result2 = UserNotFound("jacob").asLeft

  result1.fold(handleError, println) // User(jacob, qwe)
  result2.fold(handleError, println) // User not found: jacob
}

object Exercise {

  // what is best?

}
