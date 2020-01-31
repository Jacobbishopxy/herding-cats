package com.github.jacobbishopxy.herdingCats.day2

import cats._
import cats.data._
import cats.implicits._

/**
 * Created by Jacob Xie on 1/25/2020
 *
 * Functor
 *
 * The Functor typeclass is basically for things that can be mapped over.
 */
object S2 {

  Functor[List].map(List(1, 2, 3))(_ + 1)

  /**
   * We now know that @typeclass annotation will automatically turn a map function into a map operator.
   * The fa part turns into the this of the method, and the second parameter list will now be the
   * parameter list of map operator:
   */

  // Supposed generated code
  // This looks almost like the map method on Scala collection library, except this map doesn't do the
  // CanBuildFrom auto conversion.
  object FunctorGen {
    trait Ops[F[_], A] {
      def typeClassInstance: Functor[F]
      def self: F[A]
      def map[B](f: A => B): F[B] = typeClassInstance.map(self)(f)
    }
  }

  // Either as a functor
  // Cats defines a Functor instance for Either[A, B]
  (Right(1): Either[String, Int]) map (_ + 1)
  (Left("boom!"): Either[String, Int]) map (_ + 1)

  // Note that the above demonstration only works because Either[A, B] at the moment does not implements
  // its own map. Therefore, even though the operator syntax looks familiar, we should either avoid using it
  // unless you're sure that standard library doesn't implement the map or you're using it from a polymorphic
  // function. One workaround is to opt for the function syntax.

  // Function as a functor
  // Cats also defines a Functor instance for Function1.
  val h = ((x: Int) => x + 1) map (_ * 7)
  h(3)

  // Basically map gives us a way to compose functions, except the order is in reverse from f compose g.
  // Another way of looking at Function1 is that it's an infinite map from the domain to the range.

  // In Haskell, the fmap seems to be working in the same order as f compose g. Let's check in Scala using the
  // same numbers:
  (((_: Int) * 3) map (_ + 100)) (1)
  // Something is not right, the order is flipped.
  // Haskell fmap:  fmap :: (a -> b) -> f a -> f b
  // Scala cats:    def map[A, B](fa: F[A])(f: A => B): F[B]
  // That's a common Haskell-vs-Scala difference.
  //    In Haskell, to help with point-free programming, the "data" argument usually comes last. For instance,
  // I can write map f . map g . map h and get a list transformer, because the argument order is map f list.
  // (Incidentally, map is an restriction of fmap to the List functor).
  //    In Scala instead, the "data" argument is usually the receiver. That's often also important to help type
  // inference, so defining map as a method on functions would not bring you very far: think the mess Scala type
  // inference would mak of (x => x + 1) map List(1, 2, 3).

  // Lifting a function
  /**
   * LYAHFGG:
   *
   * [We can think of fmap as] a function that takes a function and returns a new function that's just like the
   * old one, only it takes a functor as a parameter and returns a functor as the result. It takes an a -> b function
   * and returns a function f a -> f b. This is called lifting a function.
   */

  // If the parameter order has been flipped, are we going to miss our on this lifting goodness?
  // Fortunately, Cats implements derived functions under the Functor typeclass.
  val lifted = Functor[List] lift ((_: Int) * 2)
  lifted(List(1, 2, 3))

  // We've just lifted the function {(_: Int) * 2} to List[Int] => List[Int]. Here the other derived functions
  // using the operator syntax:
  List(1, 2, 3).void // List[Unit] = List((), (), ())
  List(1, 2, 3) fproduct ((_: Int) * 2) // List[(Int, Int)] = List((1,2),(2,4),(3,6))
  List(1, 2, 3) as "x" // List[String] = List(x,x,x)

  /**
   * Functor Laws:
   *
   * LYAHFGG:
   *
   * In order for something to be a functor, it should satisfy some laws. All functors are expected to exhibit
   * certain kinds of functor-like properties and behaviors.
   * ...
   * The first functor law states that if we map the id function over a functor, the functor that we get back
   * should be the same as the original functor.
   */
  // We can check this for Either[A, B]
  val x: Either[String, Int] = Right(1)
  assert((x map identity) === x)

  /**
   * The second law says that composing two functions and then mapping the resulting function over a functor should
   * be the same as first mapping one function over the functor and then mapping the other one.
   */
  // In other words,
  val f = (_: Int) * 3
  val g = (_: Int) + 1
  assert((x map (f map g)) === (x map f map g))

  // These are laws the implementer of the functors must abide, and not something the compiler can check for you.

}

