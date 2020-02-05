package com.github.jacobbishopxy.theory.monads

/**
 * Created by jacob on 2/5/2020
 */
object DefiningCustomMonads {

  /**
   * We can define a Monad for a custom type by providing implementations of three methods: `flatMap`,
   * `pure`, and a method we haven't seen yer called `tailRecM`. Here is an implementation of Monad
   * for Option as an example:
   */

  import cats.Monad
  import scala.annotation.tailrec

  val optionMonad: Monad[Option] = new Monad[Option] {
    override def flatMap[A, B](fa: Option[A])(f: A => Option[B]): Option[B] =
      fa.flatMap(f)

    def pure[A](opt: A): Option[A] = Some(opt)

    @tailrec
    override def tailRecM[A, B](a: A)(f: A => Option[Either[A, B]]): Option[B] =
      f(a) match {
        case None => None
        case Some(Left(al)) => tailRecM(al)(f)
        case Some(Right(b)) => Some(b)
      }
  }

  /**
   * The `tailRecM` method is an optimisation used in Cats to limit the amount of stack space consumed
   * by nested calls to `flatMap`. The method should recursively call itself until the result of `f`
   * returns a `Right`.
   *
   * If we can make `tailRecM` tail-recursive, Cats is able to guarantee stack safety in recursive
   * situations such as folding over large lists. If we can't make `tailRecM` tail-recursive, Cats cannot
   * make these guarantees and extreme use cases may result in `StackOverflowErrors`. All of the build-in
   * monads in Cats have tail-recursive implementations of `tailRecM`, although writing one for custom
   * monads can be a challenge... as we shall see.
   */
}

object Exercise {

  // Branching our Further with Monads
  // Let's write a Monad for our Tree data type from last chapter. Here's the type again:

  sealed trait Tree[+A]

  final case class Branch[A](left: Tree[A], right: Tree[A]) extends Tree[A]
  final case class Leaf[A](value: A) extends Tree[A]

  def branch[A](left: Tree[A], right: Tree[A]): Tree[A] = Branch(left, right)
  def leaf[A](value: A): Tree[A] = Leaf(value)

  /**
   * Verify that the code works on instances of `Branch` and `Leaf`, and that the Monad provides
   * Functor-like behaviour for free.
   *
   * Also verify that having a Monad in scope allows us to use for comprehensions, despite the
   * fact that we haven't directly implemented `flatMap` or `map` on Tree.
   */

  import cats.Monad
  import scala.annotation.tailrec

  implicit val treeMonad: Monad[Tree] = new Monad[Tree] {
    def pure[A](value: A): Tree[A] = leaf(value)

    override def flatMap[A, B](fa: Tree[A])(f: A => Tree[B]): Tree[B] =
      fa match {
        case Branch(l, r) => Branch(flatMap(l)(f), flatMap(r)(f))
        case Leaf(v) => f(v)
      }

    override def tailRecM[A, B](a: A)(f: A => Tree[Either[A, B]]): Tree[B] =
      f(a) match {
        case Branch(l, r) => Branch(
          flatMap(l) {
            case Left(v) => tailRecM(v)(f)
            case Right(v) => pure(v)
          },
          flatMap(r) {
            case Left(v) => tailRecM(v)(f)
            case Right(v) => pure(v)
          }
        )
        case Leaf(Left(v)) => tailRecM(v)(f)
        case Leaf(Right(v)) => leaf(v)
      }
  }

  // test

  import cats.syntax.functor._
  import cats.syntax.flatMap._

  branch(leaf(100), leaf(200))
    .flatMap(x => branch(leaf(x - 1), leaf(x + 1)))
  //  wrapper.Tree[Int] = Branch(Branch(Leaf(99),Leaf(101)),Branch(Leaf(199),Leaf(201)))

  for {
    a <- branch(leaf(100), leaf(200))
    b <- branch(leaf(a - 10), leaf(a + 10))
    c <- branch(leaf(b - 1), leaf(b + 1))
  } yield c
  // wrapper.Tree[Int] = Branch(Branch(Branch(Leaf(89),Leaf(91)),
  //Branch(Leaf(109),Leaf(111))),Branch(Branch(Leaf(189),Leaf(191)),
  //Branch(Leaf(209),Leaf(211))))


}
