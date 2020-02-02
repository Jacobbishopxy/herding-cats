package com.github.jacobbishopxy.herdingCats.day3

import cats._
import cats.data._
import cats.implicits._

/**
 * Created by jacob on 1/31/2020
 *
 * Cartesian
 *
 * Functors, Applicative Functors and Monoids:
 *
 * >  So far, when we were mapping functions over functors, we usually mapped functions that take
 * only one parameter. But what happens when we map a function like *, which takes two parameters,
 * over a functor?
 */
object S2 {

  val hs: List[Int => Int] = Functor[List].map(List(1, 2, 3, 4)) {
    ((_: Int) * (_: Int)).curried
  }

  Functor[List].map(hs) {_ (9)}

  /**
   * LYAHFGG:
   *
   * >  But what if we have a functor value of `Just (3 *)` and a functor value of `Just 5`, and
   * we want to take out the function from `Just (3 *)` and map it over `Just 5`?
   * Meet the Applicative typeclass. It lies in the `Control.Applicative` module and it defines two
   * methods, `pure` and `<*>`.
   */

  /**
   * Cats splits this into Cartesian, Apply, and Applicative.
   *
   * Cartesian defines product function, which produces a pair of (A, B) wrapped in effect F[_]
   * out of F[A] and F[B]. The symbolic alias for product is `|@|` also known as the applicative
   * style.
   */

  // Option syntax
  9.some // Some(9): Option[Int]
  none[Int]

  /**
   * The Applicative Style
   *
   * LYAHFGG:
   *
   * >  With the Applicative typeclass, we can chain the use of the <*> function, thus enabling us to
   * seamlessly operate on several applicative values instead of just one.
   */

  // Cats comes with the CartesianBuilder syntax.
  (3.some, 5.some) mapN {_ - _} // (3.some |@| 5.some) map {_ - _}
  // return: Some(-2)

  (none[Int], 5.some) mapN {_ - _}
  // return: None

  (3.some, none[Int]) mapN {_ - _}
  // return: None

  /**
   * List as a Cartesian
   *
   * LYAHFGG:
   *
   * >  Lists (actually the list type constructor, []) are applicative functors.
   */

  // use the CartesianBuilder syntax.
  (List("ha", "heh", "hmm"), List("?", "!", ".")) mapN {_ + _}
  // return: List(ha?, ha!, ha., heh?, heh!, heh., hmm?, hmm!, hmm.)

  /**
   * > and < operators
   *
   * Cartesian enables two operators, <* and *>, which are special cases of `Apply[F].product`:
   * `
   * abstract class CartesianOps[F[_], A] extends Cartesian.Ops[F, A] {
   *  def |@|[B](fb: F[B]): CartesianBuilder[F]#CartesianBuilder2[A, B] =
   *    new CartesianBuilder[F] |@| self |@| fb
   *
   *  def *>[B](fb: F[B])(implicit F: Functor[F]): F[B] =
   *    F.map(typeClassInstance.product(self, fb)) { case (a, b) => b }
   *
   *  def <*[B](fb: F[B])(implicit F: Functor[F]): F[A] =
   *    F.map(typeClassInstance.product(self, fb)) { case (a, b) => a }
   * }
   * `
   */

  1.some <* 2.some // Some(1)
  none[Int] <* 2.some // None
  1.some *> 2.some // Some(2)
  none[Int] *> 2.some // None
  // If either side fails, we get None.

  /**
   * Cartesian law
   *
   * `
   * trait CartesianLaws[F[_]] {
   *    implicit def F: Cartesian[F]
   *
   *    def cartesianAssociativity[A, B, C](fa: F[A], fb: F[B], fc: F[C]): (F[(A, (B, C))], F[((A, B), C)]) =
   *      (F.product(fa, F.product(fb, fc)), F.product(F.product(fa, fb), fc))
   * }
   * `
   */

}
