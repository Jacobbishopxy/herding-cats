package com.github.jacobbishopxy.herdingCats.day5

/**
 * Created by jacob on 2/14/2020
 *
 * Monad
 *
 * `Monad` is a `FlatMap` with `pure`. Unlike Haskell, `Monad[F]` extends `Applicative[F]` so there’s no return vs
 * `pure` discrepancies.
 *
 * LYAHFGG:
 *
 * > Let’s say that [Pierre] keeps his balance if the number of birds on the left side of the pole and on the right
 * side of the pole is within three. So if there’s one bird on the right side and four birds on the left side,
 * he’s okay. But if a fifth bird lands on the left side, then he loses his balance and takes a dive.
 */
object S2 {

  import cats._
  import cats.data._
  import cats.implicits._

  // Now let’s try implementing the Pole example from the book.

  type Birds = Int
  case class Pole(left: Birds, right: Birds) {
    def landLeft(n: Birds): Option[Pole] =
      if (math.abs((left + n) - right) < 4) copy(left = left + n).some
      else none[Pole]
    def landRight(n: Birds): Option[Pole] =
      if (math.abs(left - (right + n)) < 4) copy(right = right + n).some
      else none[Pole]
  }

  val rlr = Monad[Option].pure(Pole(0, 0)) >>= { _.landRight(2) } >>= { _.landLeft(2) } >>= { _.landRight(2) }

  val lrlr = Monad[Option].pure(Pole(0, 0)) >>= { _.landLeft(1) } >>= { _.landRight(4) } >>= { _.landLeft(-1) } >>= { _.landRight(-2) }

  /**
   * It works. Take time to understand this example because this example highlights what a monad is.
   *
   * a. First, pure puts Pole(0, 0) into a default context: Pole(0, 0).some.
   *
   * b. Then, Pole(0, 0).some >>= {_.landLeft(1)} happens. Since it’s a Some value, _.landLeft(1) gets applied to
   * Pole(0, 0), resulting to Pole(1, 0).some.
   *
   * c. Next, Pole(1, 0).some >>= {_.landRight(4)} takes place. The result is Pole(1, 4).some. Now we at at the max
   * difference between left and right.
   *
   * d. Pole(1, 4).some >>= {_.landLeft(-1)} happens, resulting to none[Pole]. The difference is too great, and pole
   * becomes off balance.
   *
   * e. none[Pole] >>= {_.landRight(-2)} results automatically to none[Pole].
   *
   * In this chain of monadic functions, the effect from one function is carried over to the next.
   */

  /**
   * Banana on wire
   * LYAHFGG:
   *
   * > We may also devise a function that ignores the current number of birds on the balancing pole and just makes
   * Pierre slip and fall. We can call it banana.
   */

  case class Pole1(left: Birds, right: Birds) {
    def landLeft(n: Birds): Option[Pole1] =
      if (math.abs((left + n) - right) < 4) copy(left = left + n).some
      else none[Pole1]
    def landRight(n: Birds): Option[Pole1] =
      if (math.abs(left - (right + n)) < 4) copy(right = right + n).some
      else none[Pole1]
    def banana: Option[Pole1] = none[Pole1]
  }

  val lbl: Option[Pole1] = Monad[Option].pure(Pole1(0, 0)) >>= { _.landLeft(1) } >>= { _.banana } >>= { _.landRight(1) } // None

  /**
   * for comprehension
   *
   * LYAHFGG:
   *
   * > Monads in Haskell are so useful that they got their own special syntax called do notation.
   *
   * Instead of the do notation in Haskell, Scala has the for comprehension, which does similar things:
   */

  for {
    x <- 3.some
    y <- "!".some
  } yield (x.show + y) // Some(3!)

  /**
   * Pierre returns
   *
   * LYAHFGG:
   *
   * > Our tightwalker’s routine can also be expressed with do notation.
   */

  def routine: Option[Pole] =
    for {
      start <- Monad[Option].pure(Pole(0, 0))
      first <- start.landLeft(2)
      second <- first.landRight(2)
      third <- second.landLeft(1)
    } yield third

  routine // Some(Pole(3,2))

  /**
   * Pattern matching and failure
   *
   * LYAHFGG:
   *
   * > In do notation, when we bind monadic values to names, we can utilize pattern matching, just like in let
   * expressions and function parameters.
   */

  def justH: Option[Char] =
    for {
      (x :: xs) <- "hello".toList.some
    } yield x

  justH // Some(h)

  /**
   * When pattern matching fails in a do expression, the fail function is called. It’s part of the Monad type class
   * and it enables failed pattern matching to result in a failure in the context of the current monad instead of
   * making our program crash.
   */

  /**
   * Monad laws
   * Monad had three laws:
   *
   * a. left identity: (Monad[F].pure(x) flatMap {f}) === f(x)
   * b. right identity: (m flatMap {Monad[F].pure(_)}) === m
   * c. associativity: (m flatMap f) flatMap g === m flatMap { x => f(x) flatMap {g} }
   */

  /**
   * LYAHFGG:
   *
   * > The first monad law states that if we take a value, put it in a default context with return and then feed
   * it to a function by using >>=, it’s the same as just taking the value and applying the function to it.
   */

  assert {
    (Monad[Option].pure(3) >>= { x => (x + 100000).some }) ===
      ({ (x: Int) => (x + 100000).some }) (3)
  }

  /**
   * LYAHFGG:
   *
   * > The second law states that if we have a monadic value and we use >>= to feed it to return, the result is our
   * original monadic value.
   */

  assert { ("move on up".some >>= { Monad[Option].pure(_) }) === "move on up".some }

  /**
   * LYAHFGG:
   *
   * > The final monad law says that when we have a chain of monadic function applications with >>=, it shouldn’t
   * matter how they’re nested.
   */

  val l = Monad[Option].pure(Pole(0, 0)) >>= { _.landRight(2) } >>= { _.landLeft(2) } >>= { _.landRight(2) }
  val r = Monad[Option].pure(Pole(0, 0)) >>= { x =>
    x.landRight(2) >>= { y =>
      y.landLeft(2) >>= { z =>
        z.landRight(2)
      }
    }
  }

  assert(l == r)
}
