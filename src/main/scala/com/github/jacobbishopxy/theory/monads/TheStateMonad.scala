package com.github.jacobbishopxy.theory.monads

/**
 * Created by jacob on 2/5/2020
 *
 * `cats.data.State` allows us to pass additional state around as part of a computation. We define
 * `State` instances representing atomic state operations and thread them together using `map` and
 * `flatMap`. In this way we can model mutable state in a purely functional way, with our using
 * mutation.
 */
object TheStateMonad {

  /**
   * 1. Creating and Unpacking State
   *
   * Boiled down to their simplest form, instances of `State[S, A]` represent functions of type
   * `S => (A, A)`. `S` is the type of the state and `A` is the type of the result.
   */

  import cats.data.State

  val a: State[Int, String] = State[Int, String] { state =>
    (state, s"The state is $state")
  }

  /**
   * In other words, an instance of `State` is a function that does two things:
   *
   * a. transforms an input state to an output state;
   * b. computes a result.
   *
   * We can "run" our monad by supplying an initial state. `State` provides three methods -- `run`,
   * `runS` and `runA` -- that return different combinations of state and result. Each method returns
   * an instance of `Eval`, which `State` uses to maintain stack safety. We call the value method as
   * usual to extract the actual result:
   */

  val (state1, result1) = a.run(10).value
  // state1: Int = 10
  // result1: String = The state is 10

  val state2 = a.runS(10).value
  // state2: Int = 10

  val result2 = a.runA(10).value
  // result2: String = The state is 10

  /**
   * 2. Composing and Transforming State
   *
   * As we've seen with `Reader` and `Writer`, the power of the `State` monad comes from combining
   * instances. The `map` and `flatMap` methods thread the state from one instance to another. Each
   * individual instance represents an atomic state transformation, and their combination represents
   * a complete sequence of changes:
   */

  val step1 = State[Int, String] {num =>
    val ans = num + 1
    (ans, s"Result of step1: $ans")
  }

  val step2 = State[Int, String] {num =>
    val ans = num * 2
    (ans, s"Result of step2: $ans")
  }

  val both = for {
    a <- step1
    b <- step2
  } yield (a, b)

  val (state, result) = both.run(20).value
  // state: Int = 42
  // result: (String, String) = (Result of step1: 21,Result of step2: 42)

  /**
   * As you can see, in this example the final state is the result of applying both transformations
   * in sequence. State is threaded from step to step even though we don't interact with it in the
   * for comprehension.
   *
   * The general model for using the `State` monad is to represent each step of a computation as an
   * instance and compose the steps using the standard monad operators. Cats provides several
   * convenience constructors for creating primitive steps:
   *
   * a. get extracts the state as the result;
   * b. set updates the state and returns unit as the result;
   * c. pure ignores the state and returns a supplied result;
   * d. inspect extracts the state via a transformation function;
   * e. modify updates the state using an update function.
   */

  val getDemo = State.get[Int]

  getDemo.run(10).value // (Int, Int) = (10, 10)

  val setDemo = State.set[Int](30)

  setDemo.run(10).value // (Int, Unit) = (30, ())

  val pureDemo = State.pure[Int, String]("Result")

  pureDemo.run(10).value // (Int, String) = (10, Result)

  val inspectDemo = State.inspect[Int, String](_.toString + "!")

  inspectDemo.run(10).value // (Int, String) = (10, 10!)

  val modifyDemo = State.modify[Int](_ + 1)

  modifyDemo.run(10).value // (Int, Unit) = (11, ())

  /**
   * We can assemble these building blocks using a for comprehension. We typically ignore the result
   * of intermediate stages that only represent transformations on the state:
   */

  import State._

  val program: State[Int, (Int, Int, Int)] = for {
    a <- get[Int]
    _ <- set[Int](a + 1)
    b <- get[Int]
    _ <- modify[Int](_ + 1)
    c <- inspect[Int, Int](_ * 1000)
  } yield (a, b, c)

  val (s, r) = program.run(1).value
  // s: Int = 3
  // r: (Int, Int, Int) = (1,2,3000)

}

object Exercise {

  // Post-Order Calculator

}

