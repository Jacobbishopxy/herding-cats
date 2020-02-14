package com.github.jacobbishopxy.herdingCats.day5

/**
 * Created by jacob on 2/14/2020
 *
 * FlatMap
 *
 * > Monads are a natural extension applicative functors, and they provide a solution to the following problem:
 * If we have a value with context, `m` `a`, how do we apply it to a function that takes a normal `a` and returns
 * a value with a context.
 */
object S1 {

  // Note that FlatMap extends Apply, the weaker version of Applicative.

  //  class FlatMapOps[F[_], A](fa: F[A])(implicit F: FlatMap[F]) {
  //    def flatMap[B](f: A => F[B]): F[B] = F.flatMap(fa)(f)
  //    def mproduct[B](f: A => F[B]): F[(A, B)] = F.mproduct(fa)(f)
  //    def >>=[B](f: A => F[B]): F[B] = F.flatMap(fa)(f)
  //    def >>[B](fb: F[B]): F[B] = F.flatMap(fa)(_ => fb)
  //  }

  import cats._
  import cats.data._
  import cats.implicits._

  // Here’s Option as a functor:

  "wisdom".some map { _ + "!" } // Some(wisdom!)
  none[String] map { _ + "!" } // None

  // Here’s Option as an Apply:

  ({
    (_: Int) + 3
    }.some) ap 3.some // Some(6)
  none[String => String] ap "greed".some // None
  ({
    (_: String).toInt
    }.some) ap none[String] // None

  // Here’s Option as a FlatMap:

  3.some flatMap { (x: Int) => (x + 1).some } // Some(4)
  "smile".some flatMap { (x: String) => (x + " :)").some } // Some(smile :)
  none[Int] flatMap { (x: Int) => (x + 1).some } // None
  none[String] flatMap { (x: String) => (x + " :)").some } // None

  /**
   * FlatMap laws
   * FlatMap has a single law called associativity:
   *
   * associativity: (m flatMap f) flatMap g === m flatMap { x => f(x) flatMap {g} }
   */

}
