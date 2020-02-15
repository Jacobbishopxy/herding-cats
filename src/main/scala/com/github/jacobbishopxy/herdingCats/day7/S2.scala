package com.github.jacobbishopxy.herdingCats.day7

/**
 * Created by jacob on 2/15/2020
 *
 * Validated datatype
 *
 * LYAHFGG:
 *
 * > The Either e a type on the other hand, allows us to incorporate a context of possible failure to our values
 * while also being able to attach values to the failure, so that they can describe what went wrong or provide
 * some other useful info regarding the failure.
 */
object S2 {

  /**
   * We know Either[A, B] from the standard library, and we’ve covered that Cats implements a right-biased functor for it too.
   *
   * There’s another datatype in Cats that we can use in place of Either called Validated:
   */

  //  sealed abstract class Validated[+E, +A] extends Product with Serializable {
  //
  //    def fold[B](fe: E => B, fa: A => B): B =
  //      this match {
  //        case Invalid(e) => fe(e)
  //        case Valid(a) => fa(a)
  //      }
  //
  //    def isValid: Boolean = fold(_ => false, _ => true)
  //    def isInvalid: Boolean = fold(_ => true, _ => false)
  //
  //    ....
  //  }
  //
  //  object Validated extends ValidatedInstances with ValidatedFunctions{
  //    final case class Valid[+A](a: A) extends Validated[Nothing, A]
  //    final case class Invalid[+E](e: E) extends Validated[E, Nothing]
  //  }

  /**
   * What’s different about Validation is that it is does not form a monad, but forms an applicative functor.
   * Instead of chaining the result from first event to the next, Validated validates all events.
   *
   * Unlike the Xor’s monad, which cuts the calculation short, Validated keeps going to report back all failures.
   * This would be useful for validating user input on an online bacon shop.
   *
   * The problem, however, is that the error messages are mushed together into one string.
   * Shouldn’t it be something like a list?
   */

  /**
   * Using NonEmptyList to accumulate failures
   *
   * This is where NonEmptyList datatype comes in handy. For now, think of it as a list that’s guaranteed to
   * have at least one element.
   */

  import cats.data.{NonEmptyList => NEL}

  NEL.of(1) // NonEmptyList(1)

  // We can use NEL[A] on the invalid side to accumulate the errors:

  import cats.data._
  import cats.implicits._

  val result: Validated[NEL[String], String] =
    (
      Validated.valid[NEL[String], String]("event 1 ok"),
      Validated.invalid[NEL[String], String](NEL.of("event 2 failed!")),
      Validated.invalid[NEL[String], String](NEL.of("event 3 failed!"))
      ) mapN { _ + _ + _ }

  val errs: NEL[String] = result.fold(
    { l => l },
    { _ => sys.error("invalid is expected") }
  ) // NonEmptyList(event 2 failed!, event 3 failed!)

}
