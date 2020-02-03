package com.github.jacobbishopxy.theory.monoidsAndSemigroups

/**
 * Created by jacob on 2/3/2020
 *
 * Monoid and Semigroup allow us to add or combine values.
 */
object DefinitionsMonoid {

  /**
   * Definition of a Monoid
   *
   * Formally, a monoid for a type A is:
   * 1. an operation combine with type (A, A) => A
   * 2. an element empty of type A
   */
  trait Monoid[A] {
    def combine(x: A, y: A): A
    def empty: A
  }

  /**
   * In addition to providing the combine and empty operations, monoids must formally obey several laws.
   * For all values x, y, and z, in A, combine must be associative and empty must be an identity element:
   *
   */
  def associativeLaw[A](x: A, y: A, z: A)
                       (implicit m: Monoid[A]): Boolean = {
    m.combine(x, m.combine(y, z)) == m.combine(m.combine(x, y), z)
  }
  def identityLaw[A](x: A)
                    (implicit m: Monoid[A]): Boolean = {
    (m.combine(x, m.empty) == x) && (m.combine(m.empty, x) == x)
  }
}

object DefinitionsSemigroup {

  /**
   * A semigroup is just the combine part of a monoid.
   */
  trait Semigroup[A] {
    def combine(x: A, y: A): A
  }

  trait Monoid[A] extends Semigroup[A] {
    def empty: A
  }
}

object Exercise1 {

  // The Truth About Monoids

  trait Semigroup[A] {
    def combine(x: A, y: A): A
  }

  trait Monoid[A] extends Semigroup[A] {
    def empty: A
  }

  object Monoid {
    def apply[A](implicit monoid: Monoid[A]): Monoid[A] =
      monoid
  }

}

object Exercise2 {

  // All Set for Monoids

}

