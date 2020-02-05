package com.github.jacobbishopxy.theory.monads

/**
 * Created by jacob on 2/3/2020
 */
object TheIdentityMonad {

  /**
   * In the previous section we demonstrated Cat's `flatMap` and `map` syntax by writing a method
   * that abstracted over different monads (`sumSquare`).
   */

  import scala.language.higherKinds
  import cats.Monad
  import cats.syntax.functor._ // for map
  import cats.syntax.flatMap._ // for flatMap

  def sumSquare[F[_] : Monad](a: F[Int], b: F[Int]): F[Int] =
    for {
      x <- a
      y <- b
    } yield x * x + y * y

  /**
   * This method works well on Options and Lists but we can't call it passing in plain values.
   *
   * It would be incredibly useful if we could use `sumSquare` with parameters that were either
   * in a monad or not in a monad at all. This would allow us to abstract over monadic and non-monadic
   * code. Fortunately, Cats provides the Id type to bridge the gap:
   */

  import cats.Id

  sumSquare(3: Id[Int], 4: Id[Int]) // 25

  /**
   * Id allows us to call our monadic method using plain values. However, the exact semantics are
   * difficult to understand. We cast the parameters to sumSquare as Id[Int] and received an Id[Int]
   * back as a result!
   *
   * Id is actually a type alias that turns an atomic type into a single-parameter type constructor.
   * We can cast any value of any type to a corresponding Id:
   */

  val x1 = "Jacob": Id[String] // Jacob
  val x2 = 123: Id[Int] // 123
  val x3 = List(1, 2, 3): Id[List[Int]] // List(1, 2, 3)

  // Cats provides instances of various type classes for Id, including `Functor` and `Monad`. These
  // let us call `map`, `flatMap`, and `pure` passing in plain values:

  val a = Monad[Id].pure(3) // 3
  val b = Monad[Id].flatMap(a)(_ + 1) // 4

  import cats.syntax.functor._ // for map
  import cats.syntax.flatMap._ // for flatMap

  for {
    x <- a
    y <- b
  } yield x + y // 7

  /**
   * The ability to abstract over monadic and non-monadic code is extremely powerful. For example, we
   * can run code asynchronously in production using Future and synchronously in test using Id.
   */
}

object ExerciseTheIdentityMonad {

  // Monadic Secret Identities

}
