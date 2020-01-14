package com.github.jacobbishopxy.herdingCats.day0

/**
 * Created by Jacob Xie on 1/14/2020
 *
 * 2. sum function
 */
object S2 {

  // ad-hoc polymorphism by gradually making sum function more general
  // def sum(xs: List[Int]): Int = xs.foldLeft(0) {_ + _}

  // println(sum(List(1, 2, 3, 4)))

  /**
   * Monoid
   *
   * It's a type for which there exists a function mAppend, which produces another type in the same set,
   * and also a function that produces a zero
   */

  object IntMonoid {
    def mAppend(a: Int, b: Int): Int = a + b
    def mZero: Int = 0
  }

  //def sum(xs: List[Int]): Int = xs.foldLeft(IntMonoid.mZero)(IntMonoid.mAppend)
  //println(sum(List(1, 2, 3, 4)))


  // Now we'll abstract on the type about Monoid, so we can define Monoid for any type A.

  trait Monoid[A] {
    def mAppend(a1: A, a2: A): A
    def mZero: A
  }

  object IntMonoid2 extends Monoid[Int] {
    override def mAppend(a1: Int, a2: Int): Int = a1 + a2
    override def mZero: Int = 0
  }

  def sum(xs: List[Int], m: Monoid[Int]): Int = xs.foldLeft(m.mZero)(m.mAppend)

  //println(sum(List(1, 2, 3, 4, 5), IntMonoid2))

  // now replace all Int with a general type:

  //def sum[A](xs: List[A])(implicit m: Monoid[A]): A = xs.foldLeft(m.mZero)(m.mAppend)

  implicit val intMonoid: IntMonoid2.type = IntMonoid2

  //println(sum(List(1, 2, 3)))

  // in general, the implicit parameter is often written as a context bound:
  def sum[A: Monoid](xs: List[A]): A = {
    val m = implicitly[Monoid[A]]
    xs.foldLeft(m.mZero)(m.mAppend)
  }

  //println(sum(List(1, 2, 3)))

  // an object called Monoid: when it needs an implicit parameter for some type,
  // it will look for anything in scope. It'll include the companion object of
  // the type that you're looking for.
  object Monoid {
    implicit val IntMonoid: Monoid[Int] = new Monoid[Int] {
      override def mAppend(a1: Int, a2: Int): Int = a1 + a2
      override def mZero: Int = 0
    }

    implicit val StringMonoid: Monoid[String] = new Monoid[String] {
      override def mAppend(a1: String, a2: String): String = a1 + a2
      override def mZero: String = ""
    }
  }

  //println(sum(List("a", "b", "c")))

  val multiMonoid: Monoid[Int] = new Monoid[Int] {
    override def mAppend(a1: Int, a2: Int): Int = a1 * a2
    override def mZero: Int = 1
  }

}
