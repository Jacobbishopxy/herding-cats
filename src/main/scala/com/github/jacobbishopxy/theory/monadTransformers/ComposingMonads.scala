package com.github.jacobbishopxy.theory.monadTransformers

/**
 * Created by jacob on 2/5/2020
 *
 * Monads are like burritos, which means that once you acquire a taste, you'll find yourself returning
 * to them again and again. This is not without issues. As burritos can bloat the waist, monads can bloat
 * the code base through nested for-comprehensions.
 */
object ComposingMonads {

  /**
   * Imagine we are interacting with a database. We want to look up a user record. The user may or may not
   * be present, so we return an `Option[User]`. Our communication with the database could fail for many
   * reasons (network issues, authentication problems, and so on), so this result is wrapped up in an `Either`,
   * giving us a final result of `Either[Error, Option[User]]`.
   *
   * To use this value we must nest `flatMap` calls (or equivalently, for-comprehensions):
   * `
   * def lookupUserName(id: Long): Either[Error, Option[String]] =
   * for {
   * optUser <- lookupUser(id)
   * } yield {
   * for {user <- optUser} yield user.name
   * }
   * `
   *
   * This quickly becomes very tedious.
   */

  /**
   * 1. Composing Monads
   *
   * A question arise. Given two arbitrary monads, we can combine them in some way to make a single monad?
   * That is, do monads compose? We can try to write the code but we soon hit problems:
   */

  //  import cats.Monad
  //  import cats.syntax.applicative._
  //  import cats.syntax.flatMap._

  //  def compose[M1[_]: Monad, M2[_]: Monad] = {
  //    type Composed[A] = M1[M2[A]]
  //
  //    new Monad[Composed] {
  //      override def pure[A](a: A): Composed[A] = a.pure[M2].pure[M1]
  //
  //      override def flatMap[A, B](fa: Composed[A])(f: A => Composed[B]): Composed[B] = ???
  //    }
  //  }

  /**
   * It is impossible to write a general definition of `flatMap` without knowing something about `M1`
   * or `M2`. However, if we do know something about one or other monad, we can typically complete this
   * code. For example, if we fix m2 above to be Option, a definition of `flatMap` comes to light:
   */

  //    def compose[M1[_]: Monad, M2[_]: Monad] = {
  //      type Composed[A] = M1[M2[A]]
  //
  //      new Monad[Composed] {
  //        override def pure[A](a: A): Composed[A] = a.pure[M2].pure[M1]
  //
  //        override def flatMap[A, B](fa: Composed[A])
  //                                  (f: A => Composed[B]): Composed[B] =
  //          fa.flatMap(_.fold(None.pure[M])(f))
  //      }
  //    }

  /**
   * Notice that the definition above makes use of None -- an Option-specific concept that doesn't appear
   * in the general Monad interface. We need this extra detail to combine `Option` with other mondas.
   * Similarly, there are things about other monads that help us write composed `flatMap` methods for
   * them. This is the idea behind monad transformers: Cats defines transformers for a variety of monads,
   * each providing the extra knowledge we need to compose that monad with others.
   */

  /**
   * 2. A Transformative Example
   *
   * Cats provides transformers for many monads, each named with a T suffix: `EitherT` composes `Either` with
   * other monads, `OptionT` composes `Option`, and so on.
   *
   * Here's an example that uses `OptionT` to compose `List` and `Option`. We can use `OptionT[List, A]`,
   * aliased to `ListOption[A]` for convenience, to transform a `List[Option[A]]` into a single monad:
   */

  import cats.data.OptionT

  type ListOption[A] = OptionT[List, A]

  /**
   * Note how we build `ListOption` from the inside out: we pass `List`, the type of the outer monad, as
   * a parameter to `OptionT`, the transformer for the inner monad.
   *
   * We can create instances of `ListOption` using the OptionT constructor, or more conveniently using
   * `pure`:
   */

  import cats.Monad
  import cats.instances.list._
  import cats.syntax.applicative._

  val result1: ListOption[Int] = OptionT(List(Option(10)))
  // ListOption[Int] = OptionT(List(Some(10)))
  val result2: ListOption[Int] = 32.pure[ListOption]
  // ListOption[Int] = OptionT(List(Some(32)))

  // The `map` and `flatMap` methods combine the corresponding methods of `List` and `Option` into single
  // operations:
  result1.flatMap { x: Int =>
    result2.map { y: Int =>
      x + y
    }
  }
  // cats.data.OptionT[List,Int] = OptionT(List(Some(42)))

  /**
   * This is the basis of all monad transformers. The combined `map` and `flatMap` methods allow us to use
   * both component monads without having to recursively unpack and repack values at each stage in the
   * computation. Now let's look at the API in more depth.
   */

  /**
   * > Complexity of Imports
   *
   * The imports in the code samples above hint at how everything bolts together.
   *
   * We import `cats.syntax.applicative` to get the pure syntax. `pure` requires an implicit parameter
   * of type `Applicative[ListOption]`. We haven't met `Applicatives` yet, but all Monads are also
   * `Applicatives` so we can ignore that difference for now.
   *
   * In order to generate our `Applicative[ListOption]` we need instances of `Applicative` for `List`
   * and `OptionT`. `OptionT` is a Cats data type so its instance is provided by its companion object.
   * The instance for `List` comes from `cats.instances.list`.
   *
   * Notice we're not importing `cats.syntax.functor` or `cats.syntax.flatMap`. This is because `OptionT`
   * is a concrete data type with its own explicit `map` nad `flatMap` methods. It wouldn't cause problems
   * if we imported the syntax -- the compiler would ignore it in favour of the explicit methods.
   *
   * Remember that we're subjecting ourselves to these shenanigans because we're stubbornly refusing to
   * use the universal Cats import, `cats.implicits`. If we did use that import, all of the instances and
   * syntax we needed would be in scope and everything would just work.
   */

}
