package com.github.jacobbishopxy.herdingCats.day3

import cats._
import cats.data._
import cats.implicits._

/**
 * Created by jacob on 2/1/2020
 *
 * Apply
 */
object S3 {

  /**
   * Cats splits Applicative into Cartesian, Apply, and Applicative. Here's the contract for Apply:
   *
   * `
   * @typeclass(excludeParents = List("ApplyArityFunctions"))
   * trait Apply[F[_]] extends Functor[F] with Cartesian[F] with ApplyArityFunctions[F] { self =>
   *
   * def ap[A, B](ff: F[A => B])(fa: F[A]): F[B]
   *
   * ....
   * }
   * `
   *
   * Note that Apply extends Functor, Cartesian, and ApplyArityFunctions. The <*> function is called
   * `ap` in Cat's `Apply`. (This was originally called apply, but was renamed to ap.)
   *
   * LYAHFGG:
   *
   * >  You can think of <*> as a sort of a beefed-up fmap. Whereas fmap takes a function and a functor
   * and applies the function inside the functor value, <*> takes a functor that has a function in it
   * and another functor and extracts that function from the first functor and then maps it over the
   * second one.
   */

  /**
   * Option as an Apply
   *
   * Here's how we can use it with `Apply[Option].ap`:
   */

  Apply[Option].ap(((_: Int) + 3).some)(9.some)
  // return: Some(12)
  Apply[Option].ap(((_: String) + "hah").some)(none[String])
  // return: None
  Apply[Option].ap(none[String => String])("woo".some)
  // return: None
  // If either side fails, we get None.

  // Simulacrum will automatically transpose the function defined on the typeclass contract into an operator.
  {(_: Int) + 3}.some.ap(9.some)
  // return: Some(12)
  ((_: String) + "hah").some.ap(none[String])
  // return: None

  /**
   * Useful functions for Apply
   *
   * LYAHFGG:
   *
   * >  `Control.Applicative` defines a function that's called `liftA2`, which has a type of:
   * `liftA2 :: (Applicative f) => (a -> b -> c) -> f a -> f b -> f c .`
   *
   * Remember parameters are flipped around in Scala. What we have is a function that takes F[B] and
   * F[A], then a function (A, B) => C. This is called `map2` on `Apply`.
   */

  // For binary operators, `map2` can be used to hide the applicative style. Here we can write the
  // same thing in two different ways:

  (3.some, List(4).some) mapN {_ :: _}
  // return: Some(List(3, 4))
  Apply[Option].map2(3.some, List(4).some) {_ :: _}
  // return: Some(List(3, 4))

  // The 2-parameter version of `Apply[F].ap` is called `Apply[F].ap2`:
  Apply[Option].ap2(((_: Int) :: (_: List[Int])).some)(3.some, List(4).some)
  // return: Some(List(3, 4))

  // There's a special case of `map2` called `tuple2`, which works like this:
  Apply[Option].tuple2(1.some, 2.some)
  // return: Some((1,2))
  Apply[Option].tuple2(1.some, none[Int])
  // return: None

  /**
   * If you are wondering what happens when you have a function with more than two parameters, note
   * that `Apply[F[_]]` extends `ApplyArityFunctions[F]`. This is auto-generated code that defines
   * `ap3, map3, tuple3, ...` up to `ap22, map22, tuple22`.
   */

  /**
   * Apply low
   *
   * `Apply` has a single law called composition:
   *
   * `
   * trait ApplyLaws[F[_]] extends FunctorLaws[F] {
   *    implicit override def F: Apply[F]
   *
   *    def applyComposition[A, B, C](fa: F[A], fab: F[A => B], fbc: F[B => C]): IsEq[F[C]] = {
   *      val compose: (B => C) => (A => B) => (A => C) = _.compose
   *      fa.ap(fab).ap(fbc) <-> fa.ap(fab.ap(fbc.map(compose)))
   *    }
   * }
   * `
   */
}
