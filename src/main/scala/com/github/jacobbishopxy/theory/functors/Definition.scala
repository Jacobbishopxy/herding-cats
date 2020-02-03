package com.github.jacobbishopxy.theory.functors

/**
 * Created by jacob on 2/3/2020
 *
 * a class that encapsulates sequencing computations.
 * Formally, a functor is a type F[A] with an operation map with type (A => B) => F[B].
 */
object Definition {

  trait Functor[F[_]] {
    def map[A, B](fa: F[A])(f: A => B): F[B]
  }

}
