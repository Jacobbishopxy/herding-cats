package com.github.jacobbishopxy.herdingCats.day4

/**
 * Created by jacob on 2/14/2020
 *
 * Monoid
 *
 * LYAHFGG:
 * > It seems that both * together with 1 and ++ along with [] share some common properties:
 *
 * - The function takes two parameters.
 * - The parameters and the returned value have the same type.
 * - There exists such a value that does not change other values when used with the binary function.
 */
object S2 {

  /**
   * Monoid typeclass
   * Here’s the typeclass contract of algebra.Monoid:
   */

  //  /**
  //   * A monoid is a semigroup with an identity. A monoid is a specialization of a
  //   * semigroup, so its operation must be associative. Additionally,
  //   * `combine(x, empty) == combine(empty, x) == x`. For example, if we have `Monoid[String]`,
  //   * with `combine` as string concatenation, then `empty = ""`.
  //   */
  //  trait Monoid[@sp(Int, Long, Float, Double) A] extends Any with Semigroup[A] {
  //
  //    /**
  //     * Return the identity element for this monoid.
  //     */
  //    def empty: A
  //
  //      ...
  //  }

  /**
   * Monoid laws
   * In addition to the semigroup law, monoid must satisfy two more laws:
   *
   * - associativity (x |+| y) |+| z = x |+| (y |+| z)
   * - left identity Monoid[A].empty |+| x = x
   * - right identity x |+| Monoid[A].empty = x
   */


}
