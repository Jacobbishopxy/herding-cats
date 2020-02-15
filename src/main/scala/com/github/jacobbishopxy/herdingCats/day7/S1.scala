package com.github.jacobbishopxy.herdingCats.day7

/**
 * Created by jacob on 2/15/2020
 *
 * State datatype
 */
object S1 {

  /**
   * State and StateT datatype
   *
   * Learn You a Haskell for Great Good says:
   *
   * Haskell features the State monad, which makes dealing with stateful problems a breeze while still
   * keeping everything nice and pure. ….
   * We’ll say that a stateful computation is a function that takes some state and returns a value along with
   * some new state. That function would have the following type:
   *
   * `s -> (a, s)`
   *
   * State is a datatype that encapsulates a stateful computation: S => (S, A). State forms a monad
   * which passes along the states represented by the type S. Haskell should’ve named this Stater or Program to
   * avoid the confusion, but now people already know this by State, so it’s too late.
   *
   * StateT is a monad transformer, a type constructor for other data types. State partially applies
   * StateT with Eval, which emulates a call stack with heap memory to prevent overflow.
   */

  import cats._
  import cats.data._
  import cats.implicits._

  type Stack = List[Int]

  val pop: State[Stack, Int] = State[Stack, Int] {
    case x :: xs => (xs, x)
    case Nil => sys.error("stack is empty")
  }

  def push(a: Int): State[Stack, Unit] =
    State[Stack, Unit](xs => (a :: xs, ()))

  // These are the primitive programs. Now we can construct compound programs by composing the monad.

  def stackManip: State[Stack, Int] = for {
    _ <- push(3)
    _ <- pop
    b <- pop
  } yield b

  stackManip.run(List(5, 8, 2, 1)).value // (List(8, 2, 1),5)

  /**
   * The first run is for StateT, and the second is to run until the end Eval.
   *
   * Both push and pop are still purely functional, and we were able to eliminate explicitly passing
   * the state object (s0, s1, …).
   */

  /**
   * Getting and setting the state
   *
   * LYAHFGG:
   * > The Control.Monad.State module provides a type class that’s called MonadState and it features two pretty
   * useful functions, namely get and put.
   *
   * The State object defines a few helper functions:
   */

  //  private[data] abstract class StateFunctions {
  //
  //    def apply[S, A](f: S => (S, A)): State[S, A] =
  //      StateT.applyF(Now((s: S) => Now(f(s))))
  //
  //    /**
  //     * Return `a` and maintain the input state.
  //     */
  //    def pure[S, A](a: A): State[S, A] = State(s => (s, a))
  //
  //    /**
  //     * Modify the input state and return Unit.
  //     */
  //    def modify[S](f: S => S): State[S, Unit] = State(s => (f(s), ()))
  //
  //    /**
  //     * Inspect a value from the input state, without modifying the state.
  //     */
  //    def inspect[S, T](f: S => T): State[S, T] = State(s => (s, f(s)))
  //
  //    /**
  //     * Return the input state without modifying it.
  //     */
  //    def get[S]: State[S, S] = inspect(identity)
  //
  //    /**
  //     * Set the state to `s` and return Unit.
  //     */
  //    def set[S](s: S): State[S, Unit] = State(_ => (s, ()))
  //  }

  /**
   * These are confusing at first. But remember that the State monad encapsulates a pair of a state transition
   * function and a return value. So State.get keeps the state as is, and returns it.
   *
   * Similarly, State.set(s) in this context means to overwrite the state with s and return ().
   *
   * Let’s try using them with the stackyStack example from the book:
   */

  def stackyStack: State[Stack, Unit] = for {
    stackNow <- State.get[Stack]
    r <- if (stackNow === List(1, 2, 3)) State.set[Stack](List(8, 3, 1))
    else State.set[Stack](List(9, 2, 1))
  } yield r

  stackyStack.run(List(1, 2, 3)).value // (List(8, 3, 1),())

  // We can also implement both pop and push in terms of get and set:

  val pop1: State[Stack, Int] = for {
    s <- State.get[Stack]
    (x :: xs) = s
    _ <- State.set[Stack](xs)
  } yield x

  def push1(x: Int): State[Stack, Unit] = for {
    xs <- State.get[Stack]
    r <- State.set(x :: xs)
  } yield r

  /**
   * As you can see, the State monad on its own does not do much (encapsulate a state transition function
   * and a return value), but by chaining them we can remove some boilerplate.
   */

}
