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

object ExerciseTheTruthAboutMonoids {

  /**
   * We've seen a few examples of monoids but there are plenty more to be found. Consider `Boolean`. How many monoid
   * can you define for this type? For each monoid, define the `combine` and `empty` operations and convince yourself
   * that the monoid laws hold.
   */

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

  val booleanAndMonoid: Monoid[Boolean] = new Monoid[Boolean] {
    override def empty: Boolean = false
    override def combine(x: Boolean, y: Boolean): Boolean = x && y
  }

  val booleanOrMonoid: Monoid[Boolean] = new Monoid[Boolean] {
    override def empty: Boolean = false
    override def combine(x: Boolean, y: Boolean): Boolean = x || y
  }

  val booleanEitherMonoid: Monoid[Boolean] = new Monoid[Boolean] {
    override def empty: Boolean = false
    override def combine(x: Boolean, y: Boolean): Boolean = (x && !y) || (!x && y)
  }

  val booleanXorMonoid: Monoid[Boolean] = new Monoid[Boolean] {
    override def empty: Boolean = true
    override def combine(x: Boolean, y: Boolean): Boolean = (x && !y) && (!x && y)
  }

  /**
   * Note: In real world, these four conditions must be collect in one method, hence, `x`, `y`, and `condition`
   * should be input parameters.
   */
}

object ExerciseAllSetForMonoids {

  // What monoids and semigroups are there for set?

  import cats.Monoid
  import cats.Semigroup

  implicit def setUnionMonoid[A]: Monoid[Set[A]] =
    new Monoid[Set[A]] {
      override def empty: Set[A] = Set.empty[A]
      override def combine(x: Set[A], y: Set[A]): Set[A] = x.union(y)
    }

  /**
   * We need to define `setUnionMonoid` as a method rather than a value so we can accept the type parameter `A`.
   * Scala's implicit resolution is fine with this -- it is capable of determining the correct type parameter to
   * create a `Monoid` of the desired type:
   */

  implicit val intMonoid: Monoid[Int] = new Monoid[Int] {
    override def empty: Int = 0
    override def combine(x: Int, y: Int): Int = x + y
  }

  val intSetMonoid: Monoid[Set[Int]] = Monoid[Set[Int]]

  intSetMonoid.combine(Set(1, 2), Set(2, 3))

  // Set intersection forms a semigroup, but doesn't form a monoid because it has no identity element:

  implicit def setIntersectionSemigroup[A]: Semigroup[Set[A]] =
    (a: Set[A], b: Set[A]) => a.intersect(b)

  /**
   * Set complement and set difference are not associative, so they cannot be considered for either monoids
   * or semigoups. However, symmetric difference (the union less the intersection) does also form a monoid
   * with the empty set:
   */

  implicit def symDiffMonoid[A]: Monoid[Set[A]] =
    new Monoid[Set[A]] {
      override def empty: Set[A] = Set.empty
      override def combine(x: Set[A], y: Set[A]): Set[A] =
        (x.diff(y)).union(y.diff(x))
    }
}

