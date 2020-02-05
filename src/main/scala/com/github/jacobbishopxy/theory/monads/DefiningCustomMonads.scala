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

  val optionMonad = new Monad[Option] {
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

}
