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
   * `S => (S, A)`. `S` is the type of the state and `A` is the type of the result.
   */

  import cats.data.State
  import cats.Eval
  import cats.data.IndexedStateT

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

  val state2: Int = a.runS(10).value
  // state2: Int = 10

  val result2: String = a.runA(10).value
  // result2: String = The state is 10

  /**
   * 2. Composing and Transforming State
   *
   * As we've seen with `Reader` and `Writer`, the power of the `State` monad comes from combining
   * instances. The `map` and `flatMap` methods thread the state from one instance to another. Each
   * individual instance represents an atomic state transformation, and their combination represents
   * a complete sequence of changes:
   */

  val step1: State[Int, String] = State[Int, String] { num =>
    val ans = num + 1
    (ans, s"Result of step1: $ans")
  }

  val step2: State[Int, String] = State[Int, String] { num =>
    val ans = num * 2
    (ans, s"Result of step2: $ans")
  }

  val both: IndexedStateT[Eval, Int, Int, (String, String)] = for {
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

  val getDemo: State[Int, Int] = State.get[Int]

  getDemo.run(10).value // (Int, Int) = (10, 10)

  val setDemo: State[Int, Unit] = State.set[Int](30)

  setDemo.run(10).value // (Int, Unit) = (30, ())

  val pureDemo: State[Int, String] = State.pure[Int, String]("Result")

  pureDemo.run(10).value // (Int, String) = (10, Result)

  val inspectDemo: State[Int, String] = State.inspect[Int, String](_.toString + "!")

  inspectDemo.run(10).value // (Int, String) = (10, 10!)

  val modifyDemo: State[Int, Unit] = State.modify[Int](_ + 1)

  modifyDemo.run(10).value // (Int, Unit) = (11, ())

  /**
   * We can assemble these building blocks using a for comprehension. We typically ignore the result
   * of intermediate stages that only represent transformations on the state:
   */

  val program: State[Int, (Int, Int, Int)] = for {
    a <- State.get[Int]
    _ <- State.set[Int](a + 1)
    b <- State.get[Int]
    _ <- State.modify[Int](_ + 1)
    c <- State.inspect[Int, Int](_ * 1000)
  } yield (a, b, c)

  val (s, r) = program.run(1).value
  // s: Int = 3
  // r: (Int, Int, Int) = (1,2,3000)

}

object ExercisePostOrderCalculator {

  /**
   * The `State` monad allows us to implement simple interpreters for complex expressions, passing the
   * values of mutable registers along with the result. We can see a simple example of this by implementing
   * a calculator for post-order integer arithmetic expressions.
   *
   * In case you haven't heard of post-order expressions before, they are mathematical notation where we
   * write the operator after its operands. So, for example, instead of writing 1 + 2 we would write:
   * `1 2 +`
   * Although post-order expressions are difficult for humans to read, they are easy to evaluate in code.
   * All we need to do is traverse the symbols from left to right, carrying a stack of operands with use
   * as we go:
   *
   * - when we see a number, we push it onto the stack;
   * - when we see an operator, we pop two operands off the stack, operate on them, and push the result
   * in their place.
   *
   * This allows us to evaluate complex expressions without using parentheses. For example, we can evaluate
   * `(1 + 2) * 3` as follows:
   *
   * 1 2 + 3 * // see 1, push onto stack
   * 2 + 3 *   // see 2, push onto stack
   * + 3 *     // see +, pop 1 and 2 off of stack,
   * // push (1 + 2) = 3 in their place
   * 3 3 *     // see 3, push onto stack
   * 3 *       // see 3, push onto stack
   * *         // see *, pop 3 and 3 off of stack,
   * // push (3 * 3) = 9 in their place
   *
   * Let's write an interpreter for these expressions. We can parse each symbol into a `State` instance
   * representing a transformation on the stack and an intermediate result. The `State` instances can be
   * threaded together using `flatMap` to produce an interpreter for any sequence of symbols.
   *
   * Start by writing a function `evalOne` that parses a single symbol into an instance of `State`. Use
   * the code below as a template. Don't worry about error handling for now -- if the stack is in the wrong
   * configuration, it's OK to throw an exception.
   */

  import cats.data.State

  type CalcState[A] = State[List[Int], A]

  def operand(n: Int): CalcState[Int] =
    State(stack => (n :: stack, n))

  def operator(op: (Int, Int) => Int): CalcState[Int] =
    State {
      case a :: b :: tl =>
        val ans = op(a, b)
        (ans :: tl, ans)
      case _ => sys.error("Fail!")
    }

  def evalOne(sym: String): CalcState[Int] = sym match {
    case "+" => operator(_ + _)
    case "-" => operator(_ - _)
    case "*" => operator(_ * _)
    case "/" => operator(_ / _)
    case x => operand(x.toInt)
  }

  // Test

  evalOne("42").runA(Nil).value // 42

  import cats.Eval
  import cats.data.IndexedStateT

  val program: IndexedStateT[Eval, List[Int], List[Int], Int] = for {
    _ <- evalOne("1")
    _ <- evalOne("2")
    ans <- evalOne("+")
  } yield ans

  program.runA(Nil).value // 3

  /**
   * Generalise this example by writing an `evalAll` method that computes the result of a `List[String]`.
   * Use `evalOne` to process each symbol, and thread the resulting `State` monads together using `flatMap`.
   */

  import cats.syntax.applicative._

  def evalAll(input: List[String]): CalcState[Int] =
    input.foldLeft(0.pure[CalcState]) { (a, b) =>
      a.flatMap(_ => evalOne(b))
    }

  /**
   * Because `evalOne` and `evalAll` both return instances of `State`, we can thread these results together
   * using `flatMap`. `evalOne` produces a simple stack transformation and `evalAll` produces a complex one,
   * but they're both pure functions and we can use them in any order as many times as we like
   */
  val program1: IndexedStateT[Eval, List[Int], List[Int], Int] = for {
    _ <- evalAll(List("1", "2", "+"))
    _ <- evalAll(List("3", "4", "+"))
    ans <- evalOne("*")
  } yield ans

  program1.runA(Nil).value // 21

  /**
   * Complete the exercise by implementing an `evalInput` function that splits an input `String` into symbols,
   * calls `evalAll`, and runs the result with an initial stack.
   */

  def evalInput(input: String): Int =
    evalAll(input.split(" ").toList).runA(Nil).value

  // Test

  evalInput("1 2 + 3 4 + *") // 21
}

