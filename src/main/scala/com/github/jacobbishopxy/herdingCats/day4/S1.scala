package com.github.jacobbishopxy.herdingCats.day4

/**
 * Created by jacob on 2/14/2020
 *
 * Semigroup
 *
 * Haskell's `Monoid` is split in to `Semigroup` and `Monoid` in Cats. They are also type aliases of
 * `algebra.Semigroup` and `algebra.Monoid`. As with `Apply` and `Applicative`, `Semigroup` is a weaker
 * version of `Monoid`.
 *
 * LYAHFGG:
 *
 * > It doesn't matter if we do (3 * 4) * 5 or 3 * (4 * 5). Either way, the result is 60. The same goes for ++.
 * ...
 * We call this property associativity. * is associative, and so is ++, but -, for example, is not.
 *
 */
object S1 {

  import cats._
  import cats.data._
  import cats.implicits._

  assert { (3 * 2) * (8 * 5) === 3 * (2 * (8 * 5)) }
  assert { List("la") ++ (List("di") ++ List("da")) === (List("la") ++ List("di")) ++ List("da") }


  /**
   * The Semigroup typeclass
   * A semigroup is any set `A` with an associative operation (`combine`).
   */
  //  trait Semigroup[@sp(Int, Long, Float, Double) A] extends Any with Serializable {
  //
  //    /**
  //     * Associative operation taking which combines two values.
  //     */
  //    def combine(x: A, y: A): A
  //
  //      ....
  //  }

  List(1, 2, 3) |+| List(4, 5, 6)
  "one" |+| "two"

  /**
   * The Semigroup Laws
   * Associativity is the only law for Semigroup.
   *
   * associativity (x |+| y) |+| z = x |+| (y |+| z)
   */

  /**
   * Product and Sum
   * For Int a semigroup can be formed under both + and *. Instead of tagged types, cats provides only the instance additive.
   *
   * Trying to use operator syntax here is tricky.
   */

  def doSomething[A: Semigroup](a1: A, a2: A): A = a1 |+| a2

  doSomething(3, 5) // 8
  Semigroup[Int].combine(3, 5) // 8
}
