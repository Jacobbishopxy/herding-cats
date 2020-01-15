package com.github.jacobbishopxy.herdingCats.day0

import simulacrum._

/**
 * Created by Jacob Xie on 1/14/2020
 *
 * 4. Method injection
 */
object S4 {

  // we would like to provide an operator. we don't want to enrich just one type,
  // but enrich all types that has an instance for Monoid.


  @typeclass trait Monoid[A] {
    @op("|+|") def mAppend(a: A, b: A): A
    def mZero: A
  }

  object Monoid {
    // "ops" gets generated
    val syntax: ops.type = ops

    implicit val IntMonoid: Monoid[Int] = new Monoid[Int] {
      override def mAppend(a: Int, b: Int): Int = a + b
      override def mZero: Int = 0
    }

    implicit val StringMonoid: Monoid[String] = new Monoid[String] {
      override def mAppend(a: String, b: String): String = a + b
      override def mZero: String = ""
    }
  }

  import Monoid.syntax._

  //println(3 |+| 4)
  //println("a" |+| "b")


}
