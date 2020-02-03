package com.github.jacobbishopxy.theory.monoidsAndSemigroups

/**
 * Created by jacob on 2/3/2020
 *
 * the three main aspects of the implementation:
 * 1. the type class
 * 2. the instances
 * 3. the interface
 */
object MonoidsInCats extends App {

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

  val stringResult = "Hi" |+| "there" |+| Monoid[String].empty
  println(stringResult)

}

object Exercise {

  // Adding All The Things

}
