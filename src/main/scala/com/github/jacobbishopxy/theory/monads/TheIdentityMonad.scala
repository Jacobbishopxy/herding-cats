package com.github.jacobbishopxy.theory.monads

/**
 * Created by jacob on 2/3/2020
 */
object TheIdentityMonad {

  /**
   * In the previous section we demonstrated Cat's `flatMap` and `map` syntax by writing a method
   * that abstracted over different monads (`sumSquare`).
   */

  // import scala.language.higherKinds // no longer import explicitly since Scala 2.13.1
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

  // Implement `pure`, `map` and `flatMap` for `Id`! What interesting discoveries do you uncover about
  // the implementation?

  import cats.Id

  /**
   * The `pure` operation creates an `Id[A]` from `A`. But `A` and `Id[A]` are the same type.
   */
  def pure[A](value: A): Id[A] = value

  /**
   * The `map` method takes a parameter of type `Id[A]`, applies a function of type `A => B`, and returns
   * an `Id[B]`. But `Id[A]` is simply `A` and `Id[B]` is simply `B`. All we have to do is call the function
   * -- no packing or unpacking required.
   */
  def map[A, B](initial: Id[A])(func: A => B): Id[B] =
    func(initial)

  /**
   * Once we strip away the `Id` type constructors, `flatMap` and `map` are actually identical.
   */
  def flatMap[A, B](initial: Id[A])(func: A => Id[B]): Id[B] =
    func(initial)

  /**
   * This ties in with our understanding of functors and monads as sequencing type classes. Each type class
   * allows us to sequence operations ignoring some kind of complication. In the case of `Id` there is no
   * complication, making `map` and `flatMap` the same thing.
   *
   * Notice that we haven't had to write type annotations in the method bodies above. The compiler is able to
   * interpret values of type `A` as `Id[A]` and vice versa by the context in which they are used.
   *
   * The only restriction we've seen to this is that Scala cannot unify types and type constructors when
   * searching for implicits. Hence our need to re-type `Int` as `Id[Int]` in the call to `sumSquare` at the
   * opening of this section:
   *
   * `sumSquare(3 : Id[Int], 4 : Id[Int])`
   */
}
