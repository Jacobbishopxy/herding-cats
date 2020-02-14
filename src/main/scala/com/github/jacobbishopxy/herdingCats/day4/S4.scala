package com.github.jacobbishopxy.herdingCats.day4

/**
 * Created by jacob on 2/14/2020
 *
 * Using monoids to fold data structures
 *
 * LYAHFGG:
 *
 * > Because there are so many data structures that work nicely with folds, the Foldable type class was introduced.
 * Much like Functor is for things that can be mapped over, Foldable is for things that can be folded up!
 */
object S4 {

  import cats._
  import cats.data._
  import cats.implicits._

  Foldable[List].foldLeft(List(1, 2, 3), 1) { _ * _ }

  /**
   * `Foldable` comes with some useful functions/operators, many of them taking advantage of the typeclasses. Let’s
   * try the `fold. Monoid[A]` gives us empty and `combine`, so that’s enough information to fold over things.
   */

  Foldable[List].fold(List(1, 2, 3))(Monoid[Int]) // 6

  List(1, 2, 3).foldMap(identity)(Monoid[Int]) // 6

  // Another useful thing is that we can use this to convert the values into new type.

  class Conjunction(val unwrap: Boolean) extends AnyVal

  object Conjunction {
    def apply(b: Boolean): Conjunction = new Conjunction(b)

    implicit val conjunctionMonoid: Monoid[Conjunction] = new Monoid[Conjunction] {
      override def combine(x: Conjunction, y: Conjunction): Conjunction =
        Conjunction(x.unwrap && y.unwrap)

      override def empty: Conjunction = Conjunction(true)
    }
  }

  val x: Conjunction = List(true, false, true).foldMap(Conjunction(_))

  val xu: Boolean = x.unwrap // false

}
