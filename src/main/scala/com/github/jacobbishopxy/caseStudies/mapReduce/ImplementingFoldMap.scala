package com.github.jacobbishopxy.caseStudies.mapReduce

/**
 * Created by Administrator on 2/17/2020
 */
object ImplementingFoldMap {

  /**
   * We saw `foldMap` briefly back when we covered `Foldable`. It is one of the derived operations that sits on top of
   * `foldLeft` and `foldRight`. However, rather than use `Foldable`, we will re-implement `foldMap` here ourselves
   * as it will provide useful insight into the structure of map-reduce.
   *
   * Start by writing out the signature of `foldMap`. It should accept the following parameters:
   *
   * a. a sequence of type `Vector[A]`
   * b. a function of type `A => B`, where there is a `Monoid` for `B`
   *
   * You will have to add implicit parameters or context bounds to complete the type signature.
   */

  import cats.Monoid

  def foldMap0[A, B: Monoid](d: Vector[A], fn: A => B): B = ???

  /**
   * Now implement the body of `foldMap`.
   *
   * a. start with a sequence of items of type `A`
   * b. map over the list to produce a sequence of items of type B
   * c. use the `Monoid` to reduce the items to a single B
   */

  import cats.syntax.semigroup._ // for |+|

  def foldMap[A, B: Monoid](d: Vector[A], fn: A => B): B =
    d.map(fn).foldLeft(Monoid[B].empty)(_ |+| _)

}

//object ImplementingFoldMapTest extends App {
//
//  import ImplementingFoldMap.foldMap
//
//  import cats.instances.string._
//
//  val f: Int => String = (v: Int) => (v * 2).toString + "!"
//
//  println(foldMap(Vector(1, 2, 3), f))
//
//}
