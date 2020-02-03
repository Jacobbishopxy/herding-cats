package com.github.jacobbishopxy.theory.functors

/**
 * Created by jacob on 2/3/2020
 *
 */
object FunctorsInCats extends App {

  // 1. the type class

  import cats.Functor

  // 2. the instances

  import cats.instances.list._
  import cats.instances.option._

  val list1 = List(1, 2, 3)
  val list2 = Functor[List].map(list1)(_ * 2)

  val option1 = Option(123)
  val option2 = Functor[Option].map(option1)(_.toString)

  // Functor also provides the lift method, which converts a function of type A => B to one
  // that operates over a functor and has type F[A] => F[B]:
  val func = (x: Int) => x + 1
  val liftedFunc = Functor[Option].lift(func)

  liftedFunc(Option(1)) // Some(2)

  // 3. Functor Syntax
  // The main method provided by the syntax for Functor is map.

  import cats.instances.function._
  import cats.syntax.functor._

  val func1 = (a: Int) => a + 1
  val func2 = (a: Int) => a * 1
  val func3 = (a: Int) => a.toString + "!"
  val func4 = func1.map(func2).map(func3)

  func4(123) // "248!"

  // another example: we'll abstract over functors so we're not working with any particular concrete type.
  def doMath[F[_]](start: F[Int])(implicit functor: Functor[F]): F[Int] =
    start.map(n => n + 2)

  doMath(Option(20)) // Some(22)
  doMath(List(1, 2, 3)) // List(3, 4, 5)

  // To illustrate how this works, let's take a look at the definition of the map method in cats.syntax.functor:
  implicit class MyFunctorOps[F[_], A](src: F[A]) {
    def myMap[B](func: A => B)(implicit functor: Functor[F]): F[B] =
      functor.map(src)(func)
  }
  /**
   * The compiler can use this extension method to insert a map method wherever no build-in map is available:
   * `foo.map(value => value + 1)`
   *
   * Assuming foo has no built-in map method, the compiler detects the potential error and wraps the expression
   * in a `FunctorOps` to fix the code:
   * `new FunctorOps(foo).map(value => value + 1)`
   *
   * The map method of `FunctorOps` requires an implicit `Functor` as a parameter. This means this code will
   * only compile if we have a `Functor` for `expr1` in scope.
   */
}

object Exercise {

  // Branching out with Functors

  sealed trait Tree[+A]

  final case class Branch[A](left: Tree[A], right: Tree[A]) extends Tree[A]

  final case class Leaf[A](value: A) extends Tree[A]

  import cats.Functor
  import cats.syntax.functor._

  implicit val treeFunctor: Functor[Tree] =
    new Functor[Tree] {
      override def map[A, B](fa: Tree[A])(f: A => B): Tree[B] = fa match {
        case Branch(l, r) => Branch(map(l)(f), map(r)(f))
        case Leaf(v) => Leaf(f(v))
      }
    }

  /**
   * The compiler can find a Functor instance for `Tree` but not for `Branch` or `Leaf`.
   * Let's add some smart constructors to compensate:
   */
  object Tree {
    def branch[A](left: Tree[A], right: Tree[A]): Tree[A] =
      Branch(left, right)

    def leaf[A](value: A): Tree[A] =
      Leaf(value)
  }

  Tree.leaf(100).map(_ * 2) // Leaf(200)
  Tree.branch(Tree.leaf(10), Tree.leaf(20)).map(_ * 2) // Branch(Leaf(20), Leaf(40))

}
