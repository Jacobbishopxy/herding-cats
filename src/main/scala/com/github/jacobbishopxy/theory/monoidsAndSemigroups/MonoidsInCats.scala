package com.github.jacobbishopxy.theory.monoidsAndSemigroups

/**
 * Created by jacob on 2/3/2020
 *
 * the three main aspects of the implementation:
 * 1. the type class
 * 2. the instances
 * 3. the interface
 */
object MonoidsInCats {

  // 1. The Monoid Type Class

  import cats.Monoid
  import cats.Semigroup

  // 2. Monoid Instances
  import cats.instances.string._

  Monoid[String].combine("Hi", "there") // equivalent to:
  Monoid.apply[String].combine("Hi", "there")

  Monoid[String].empty // equivalent to:
  Monoid.apply[String].empty

  Semigroup[String].combine("Hi", "there")

  // 3. Monoid Syntax

  import cats.syntax.semigroup._

  "Hi" |+| "there" |+| Monoid[String].empty

}

object ExerciseAddingAllTheThings {

  // 0.
  def add0(items: List[Int]): Int = items.foldLeft(0)(_ + _)

  import cats.Monoid
  import cats.instances.int._ // for Monoid
  import cats.syntax.semigroup._ // for |+|

  // 1.
  def add1(items: List[Int]): Int =
    items.foldLeft(Monoid[Int].empty)(_ |+| _)

  // 2.
  def add2[A](items: List[A])(implicit monoid: Monoid[A]): A =
    items.foldLeft(monoid.empty)(_ |+| _)

  // 3.

  case class Order(totalCost: Double, quantity: Double)

  implicit val monoid: Monoid[Order] = new Monoid[Order] {
    override def empty: Order = Order(0, 0)
    override def combine(x: Order, y: Order): Order =
      Order(x.totalCost + y.totalCost, x.quantity + y.quantity)
  }
}
