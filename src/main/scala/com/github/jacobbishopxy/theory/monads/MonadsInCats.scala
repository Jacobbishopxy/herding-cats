package com.github.jacobbishopxy.theory.monads

/**
 * Created by jacob on 2/3/2020
 */
object MonadsInCats {

  /**
   * 1. The Monad Type class
   *
   * The monad type class is `cats.Monad`. Monad extends two other type classes: `FlatMap`, which
   * provides the flatMap method, and `Applicative`, which provides pure. Applicative also extends
   * Functor, which gives every Monad a map method.
   */

  // some examples using `pure` and `flatMap`, and `map` directly:

  import cats.Monad
  import cats.instances.option._
  import cats.instances.list._

  val opt1 = Monad[Option].pure(3) // Some(3)
  val opt2 = Monad[Option].flatMap(opt1)(a => Some(a + 2)) // Some(5)
  val opt3 = Monad[Option].map(opt2)(a => 100 * a) // Some(500)

  val lst1 = Monad[List].pure(3) // List(3)
  val lst2 = Monad[List].flatMap(List(1, 2, 3))(a => List(a, a * 10)) // List(1, 10, 2, 20, 3, 30)
  val lst3 = Monad[List].map(lst2)(a => a + 123) // List(124, 133, 125, 143, 126, 153)

  /**
   * 2. Default Instances
   *
   * Cats provides instances for all the monads in the standard library (Option, List, Vector and so on)
   * via `cats.instances`:
   */

  import cats.instances.vector._

  Monad[Vector].flatMap(Vector(1, 2, 3))(a => Vector(a, a * 10)) // Vector(1, 10, 2, 20, 3, 30)

  /**
   * Cats also provides a Monad for Future. Unlike the methods on the Future class itself, the pure
   * and flatMap methods on the monad can't accept implicit `ExecutionContext` parameters (because
   * the parameters aren't part of the definitions in the Monad trait). To work around this, Cats
   * requires us to have an `ExecutionContext` in scope when summon a Monad for `Future`:
   */

  import cats.instances.future._
  import scala.concurrent._
  import scala.concurrent.duration._

  // Bringing the `ExecutionContext` into scope fixes the implicit resolution required to summon the
  // instance:
  import scala.concurrent.ExecutionContext.Implicits.global

  val fm = Monad[Future]

  // The Monad instance uses the captured `ExecutionContext` for subsequent calls to pure and flatMap:
  val future = fm.flatMap(fm.pure(1))(x => fm.pure(x + 2))

  Await.result(future, 1.second)

}

object MonadSyntax {

  /**
   * 3. Monad Syntax
   *
   * The syntax for monads comes from three places:
   *
   * a. `cats.syntax.flatMap` provides syntax for flatMap;
   * b. `cats.syntax.functor` provides syntax for map;
   * c. `cats.syntax.applicative` provides syntax for pure.
   */

  /**
   * In practice it's often easier to import everything in one go from `cats.implicits`. However, we'll
   * use the individual imports here for clarity.
   *
   * We can use pure to construct instances of a monad. We'll often need to specify the type parameter
   * to disambiguate the particular instance we want.
   */

  import cats.instances.option._ // for Monad
  import cats.instances.list._ // for Monad
  import cats.syntax.applicative._ // for pure

  1.pure[Option] // Some(1)
  1.pure[List] // List(1)

  /**
   * It's difficult to demonstrate the flatMap and map methods directly on Scala monads like `Option` and
   * `List`, because they define their own explicit versions of those methods. Instead we'll write a
   * generic function that performs a calculation on parameters that come wrapped in a monad of the
   * use's choice:
   */

  import cats.Monad
  import cats.syntax.functor._ // for map
  import cats.syntax.flatMap._ // for flatMap

  def sumSquare[F[_]: Monad](a: F[Int], b: F[Int]): F[Int] =
    a.flatMap(x => b.map(y => x * x + y * y))

  import cats.instances.option._ // for Monad
  import cats.instances.list._ // for Monad

  sumSquare(Option(3), Option(4)) // Some(25)
  sumSquare(List(1, 2, 3), List(4, 5)) // List(17, 26, 20, 29, 25, 34)

  /**
   * We can rewrite this code using for comprehensions. The compiler will "do the right thing" by rewriting
   * our comprehension in terms of `flatMap` and `map` and inserting the correct implicit conversions to use
   * our Monad:
   */

  def sumSquarePro[F[_]: Monad](a: F[Int], b: F[Int]): F[Int] =
    for {
      x <- a
      y <- b
    } yield x * x + y * y

  sumSquarePro(Option(3), Option(4)) // Some(25)
  sumSquarePro(List(1, 2, 3), List(4, 5)) // List(17, 26, 20, 29, 25, 34)
}
