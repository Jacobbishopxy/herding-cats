package com.github.jacobbishopxy.herdingCats.day2

import simulacrum._

/**
 * Created by Jacob Xie on 1/15/2020
 *
 * Making our own typeclass with simulacrum
 *
 * LYAHFGG:
 * In JavaScript and some other weakly typed languages, you can put almost anything inside an if expression.
 * Even though strictly using Bool for boolean semantics works better in Haskell, let's try and implement that
 * JavaScript-ish behavior anyway.
 *
 * The conventional steps of defining a modular typeclass in Scala used to look like:
 * 1. Define typeclass contract trait Foo.
 * 2. Define a companion object Foo with a helper method apply that acts like implicitly, and a way of defining
 * Foo instances typically from a function.
 * 3. Define FooOps class that defines unibary or binary operators.
 * 4. Define FooSyntax trait that implicitly provides FooOps from a Foo instance.
 *
 * Frankly, these steps are mostly copy-paste boilerplate except for the first one.
 */
object S1 {

  // Yes-No typeclass

  @typeclass trait CanTruthy[A] {
    self =>
    // Return true, if `a` is truthy
    def truthy(a: A): Boolean
  }

  object CanTruthy {
    def fromTruthy[A](f: A => Boolean): CanTruthy[A] =
      (a: A) => f(a)
  }

  //let's define an instance for Int and use it. The eventual goal is to get 1.truthy to return true:
  implicit val intCanTruthy: CanTruthy[Int] = CanTruthy.fromTruthy({
    case 0 => false
    case _ => true
  })

  import CanTruthy.ops._

  10.truthy // true

  /**
   * Symbolic operators
   *
   * For CanTruthy the injected operator happened to be unary, and it matched the name of the function on the
   * typeclass contract. Simulacrum can also define operator with symbolic names using @op annotation:
   */
  @typeclass trait CanAppend[A] {
    @op("|+|") def append(a1: A, a2: A): A
  }

  implicit val inCanAppend: CanAppend[Int] = (a1: Int, a2: Int) => a1 + a2

  import CanAppend.ops._

  1 |+| 2 // 3
}

// the macro will generate all the operator enrichment
//object CanTruthyAutoGen {
//
//  object CanTruthy {
//    def fromTruthy[A](f: A => Boolean): CanTruthy[A] = new CanTruthy {
//      def truthy(a: A): Boolean = f(a)
//    }
//
//    def apply[A](implicit instance: CanTruthy[A]): CanTruthy[A] = instance
//
//    trait Ops[A] {
//      def typeClassInstance: CanTruthy[A]
//      def self: A
//      def truthy: A = typeClassInstance.truthy(self)
//    }
//
//    trait ToCanTruthyOps {
//      implicit def toCanTruthyOps[A](target: A)(implicit tc: CanTruthy[A]): Ops[A] =
//        new Ops[A] {
//          val self = target
//          val typeClassInstance = tc
//        }
//    }
//
//    trait AllOps[A] extends Ops[A] {
//      def typeClassInstance: CanTruthy[A]
//    }
//
//    object ops {
//      implicit def toAllCanTruthyOps[A](target: A)(implicit tc: CanTruthy[A]): AllOps[A] =
//        new AllOps[A] {
//          val self = target
//          val typeClassInstance = tc
//        }
//    }
//  }
//
//}
