package com.github.jacobbishopxy.herdingCats.day7

/**
 * Created by jacob on 2/15/2020
 *
 * Ior datatype
 */
object S3 {

  // In Cats there is yet another datatype that represents an A-B pair called Ior.

  import cats._
  import cats.data._
  import cats.implicits._

  Ior.right[NonEmptyList[String], Int](1) // Right(1)
  Ior.left[NonEmptyList[String], Int](NonEmptyList.of("error")) // Left(NonEmptyList(error))
  Ior.both[NonEmptyList[String], Int](NonEmptyList.of("warning"), 1) // Both(NonEmptyList(warning),1)

  /**
   * As noted in the scaladoc comment, Ior’s flatMap uses Semigroup[A] to accumulate failures when it sees
   * an Ior.both(...) value. So we could probably use this as a hybrid of Xor and Validated.
   *
   * Here’s how flatMap behaves for all nine combinations:
   */

  Ior.right[NonEmptyList[String], Int](1) >>=
    { x => Ior.right[NonEmptyList[String], Int](x + 1) }
  // Right(2)

  Ior.left[NonEmptyList[String], Int](NonEmptyList.of("error 1")) >>=
    { x => Ior.right[NonEmptyList[String], Int](x + 1) }
  // Left(NonEmptyList(error 1))

  Ior.both[NonEmptyList[String], Int](NonEmptyList.of("warning 1"), 1) >>=
    { x => Ior.right[NonEmptyList[String], Int](x + 1) }
  // Both(NonEmptyList(warning 1),2)


  Ior.right[NonEmptyList[String], Int](1) >>=
    { x => Ior.left[NonEmptyList[String], Int](NonEmptyList.of("error 2")) }
  // Left(NonEmptyList(error 2))

  Ior.left[NonEmptyList[String], Int](NonEmptyList.of("error 1")) >>=
    { x => Ior.left[NonEmptyList[String], Int](NonEmptyList.of("error 2")) }
  // Left(NonEmptyList(error 1))

  Ior.both[NonEmptyList[String], Int](NonEmptyList.of("warning 1"), 1) >>=
    { x => Ior.left[NonEmptyList[String], Int](NonEmptyList.of("error 2")) }
  // Left(NonEmptyList(warning 1, error 2))


  Ior.right[NonEmptyList[String], Int](1) >>=
    { x => Ior.both[NonEmptyList[String], Int](NonEmptyList.of("warning 2"), x + 1) }
  // Both(NonEmptyList(warning 2),2)

  Ior.left[NonEmptyList[String], Int](NonEmptyList.of("error 1")) >>=
    { x => Ior.both[NonEmptyList[String], Int](NonEmptyList.of("warning 2"), x + 1) }
  // Left(NonEmptyList(error 1))

  Ior.both[NonEmptyList[String], Int](NonEmptyList.of("warning 1"), 1) >>=
    { x => Ior.both[NonEmptyList[String], Int](NonEmptyList.of("warning 2"), x + 1) }
  // Both(NonEmptyList(warning 1, warning 2),2)


  // Let’s try using it in for comprehension:
  for {
    e1 <- Ior.right[NonEmptyList[String], Int](1)
    e2 <- Ior.both[NonEmptyList[String], Int](NonEmptyList.of("event 2 warning"), e1 + 1)
    e3 <- Ior.both[NonEmptyList[String], Int](NonEmptyList.of("event 3 warning"), e2 + 1)
  } yield e1 |+| e2 |+| e3
  // Both(NonEmptyList(event 2 warning, event 3 warning),6)

  // So Ior.left short circuits like the failure values in Xor[A, B] and Either[A, B],
  // but Ior.both accumulates the failure values like Validated[A, B].

}
