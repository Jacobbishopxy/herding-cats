package com.github.jacobbishopxy.herdingCats.day0

/**
 * Created by Jacob Xie on 1/14/2020
 *
 * 1. What is polymorphism?
 */
object S1 {

  /**
   * a. Parametric polymorphism
   */

  def head[A](xs: List[A]): A = xs(0)

  //println(head(1 :: 2 :: Nil))

  case class Car(make: String)

  //println(head(Car("Civic") :: Car("CR-V") :: Nil))

  /**
   * Haskell wiki:
   *
   * Parametric polymorphism refers to when then type of a value contains one or more (unconstrained)
   * type variables, so that the value may adopt any type that results from substituting those variables
   * with concrete types.
   */

  /**
   * b. Subtype polymorphism
   */

  // Let's think of a function plus that can add two values of type A:
  //def plus[A](a1: A, a2: A): A = ???

  // Depending on the type A, we need to provide different definition for what it means to add them.
  // One way to achieve this is through subtyping.

  trait PlusIntf[A] {
    def plus(a2: A): A
  }

  def plusBySubtype[A <: PlusIntf[A]](a1: A, a2: A): A = a1.plus(a2)

  // We can at least provide different definitions of plus for A. But, this is not flexible
  // since trait Plus needs to be mixed in at the time of defining the datatype.
  // So it can't work for Int and String.

  /**
   * c. Ad-hoc polymorphism
   */

  // The third approach in Scala is to provide an implicit conversion or implicit parameters for the trait.
  trait CanPlus[A] {
    def plus(a1: A, a2: A): A
  }

  def plus[A: CanPlus](a1: A, a2: A): A = implicitly[CanPlus[A]].plus(a1, a2)

  // This is truly ad-hoc in the sense that
  // 1. we can provide separate function definitions for different types of A
  // 2. we can provide function definitions to types (like Int) without access to its source code
  // 3. the function definitions can be enabled or disabled in different scopes


  // example:
  implicit val plusInt: CanPlus[Int] = (a1: Int, a2: Int) => a1 + a2
  //println(plus(1, 2))
}
