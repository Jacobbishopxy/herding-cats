package com.github.jacobbishopxy.herdingCats.day3

import cats._
import cats.data._
import cats.implicits._

/**
 * Created by jacob on 2/2/2020
 *
 * Applicative
 */
object S4 {

  /**
   * Cat's `Applicative`:
   *
   * `
   * @typeclass trait Applicative[F[_]] extends Apply[F] { self =>
   * def pure[A](x: A): F[A]
   * ....
   * }
   * `
   *
   * It's an extension of `Apply` with `pure`.
   *
   * LYAHFGG:
   *
   * >  pure should take a value of any type and return an applicative value with that value inside it.
   * ... A better way of thinking about pure would be to say that it takes a value and puts it in some
   * sort of default (or pure) context -- a minimal context that still yields that value.
   */

  // It seems like it's basically a constructor that takes value A and returns F[A].
  Applicative[List].pure(1) // List(1)
  Applicative[Option].pure(1) // Some(1)

  // This actually comes in handy using `Apply[F].ap` so we can avoid calling `{{...}.some}`.
  val F: Applicative[Option] = Applicative[Option]
  F.ap(F.pure((_: Int) + 3))(F.pure(9)) // Some(12)
  // We've abstracted `Option` away from the code.

  /**
   * Useful functions for Applicative
   *
   * LYAHFGG:
   *
   * >  Let's try implementing a function that takes a list of applicatives and returns an applicative
   * that has a list as its result value. We'll call it `sequenceA`.
   */

  // Let's try implementing this with Cats!
  def sequenceA[F[_] : Applicative, A](list: List[F[A]]): F[List[A]] =
    list match {
      case Nil => Applicative[F].pure(Nil: List[A])
      case x :: xs => (x, sequenceA(xs)) mapN {_ :: _}
    }

  sequenceA(List(1.some, 2.some)) // Some(List(1, 2))
  sequenceA(List(3.some, none[Int], 1.some)) // None
  sequenceA(List(List(1, 2, 3), List(4, 5, 6))) // List(List(1,4),List(1,5),List(1,6),List(2,4),List(2,5),List(2,6),List(3,4),List(3,5),List(3,6))

  /**
   * What's interesting here is that we did end up needing `Applicative` after all, and `sequenceA` is
   * generic in a typeclassy way.
   *
   * >  Using sequenceA is useful when we have a list of functions and we want to feed the same input to
   * all of them and then view the list of results.
   */

  /**
   * Applicative Laws
   *
   * Here are the laws for `Applicative`:
   *
   * 1. identity: pure id <*> v = v
   * 2. homomorphism: pure f <*> pure x = pure (f x)
   * 3. interchange: u <*> pure y = pure ($ y) <*> u
   *
   * Cats defines another law
   *
   * `
   * def applicativeMap[A, B](fa: F[A], f: A => B): IsEq[F[B]] =
   *     fa.map(f) <-> fa.ap(F.pure(f))
   * `
   */
}
