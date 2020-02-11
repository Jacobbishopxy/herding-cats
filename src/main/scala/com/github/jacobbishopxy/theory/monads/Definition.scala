package com.github.jacobbishopxy.theory.monads

/**
 * Created by jacob on 2/3/2020
 *
 * A monad is a mechanism for sequencing computations.
 *
 * Monadic behaviour is formally captured in two operations:
 * 1. pure, of type A => F[A]
 * 2. flatMap, of type (F[A], A => F[B]) => F[B]
 *
 * `pure` abstracts over constructors, providing a way to create a new monadic context from a plain value.
 * `flatMap` provides the sequencing step, extracting the value from a context and generating the next
 * context in the sequence.
 */
object Definition {

  // a simplified version of the Monad type class in Cats:
  trait Monad[F[_]] {
    def pure[A](value: A): F[A]

    def flatMap[A, B](value: F[A])(func: A => F[B]): F[B]
  }

  /**
   * Monad Laws
   *
   * `pure` and `flatMap` must obey a set of laws that allow us to sequence operations freely without
   * unintended glitches and side-effects:
   *
   * 1. Left identity: calling pure and transforming the result with func is the same as calling func:
   * `pure(a).flatMap(func) == func(a)`
   * 2. Right identity: passing pure to flatMap is the same as doing nothing:
   * `m.flatMap(pure) == m`
   * 3. Associativity: flatMapping over two functions f and g is the same as flatMapping over f and
   * then flatMapping over g
   * `m.flatMap(f).flatMap(g) == m.flatMap(x => f(x).flatMap(g))`
   */
}

object ExerciseDefinition {

  // Getting Func-y

  // Every monad is also a functor. We can define map in the same way for every monad using the existing
  // methods, `flatMap` and `pure`:
  trait Monad[F[_]] {
    def pure[A](a: A): F[A]

    def flatMap[A, B](value: F[A])(func: A => F[B]): F[B]

    def map[A, B](value: F[A])(func: A => B): F[B] =
      flatMap(value)(v => pure(func(v)))
  }

}
