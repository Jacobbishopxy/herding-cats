package com.github.jacobbishopxy.theory.monads

/**
 * Created by jacob on 2/4/2020
 *
 * `cats.Eval` is a monad that allows us to abstract over different models of evaluation. We typically
 * hear of two such models: `eager` and `lazy`. Eval throws in a further distinction of whether or not
 * a result is memorized.
 */
object TheEvalMonad {

  /**
   * 1. Eager, Lazy, Memoized
   *
   * `Eager` computations happen immediately whereas `lazy` computations happen on access. `Memoized`
   * computations are run once on first access, after which the results are cached.
   *
   * For example, Scala `val`s are eager and memoized. We can see this using a computation with a visible
   * side-effect. By contrast, `def`s are lazy and not memoized. Last but not least, `lazy val`s are
   * lazy and memoized.
   */

  /**
   * 2. Eval's Models of Evaluation
   *
   * Eval has three subtypes: `Now`, `Later`, and `Always`. We construct these with three constructor
   * methods, which create instances of the three classes and return them typed as `Eval`:
   */

  import cats.Eval

  val now: Eval[Double] = Eval.now(math.random + 1000)
  val later: Eval[Double] = Eval.later(math.random + 2000)
  val always: Eval[Double] = Eval.always(math.random + 3000)

  // We can extract the result of an Eval using its value method:
  now.value
  later.value
  always.value

  // Each type of Eval calculates its result using one of the evaluation models defined above.
  // `Eval.now` captures a value right now. Its semantics are similar to a `val` -- eager and memoized.
  // `Eval.always` captures a lazy computation, similar to a `def`
  // `Eval.later` captures a lazy, memoized computation, similar to a `lazy val`

  /**
   * 3. Eval as a Monad
   *
   * Like all monads, Eval's `map` and `flatMap` methods add computations to a chain. In this case,
   * however, the chain is stored explicitly as a list of functions. The functions aren't run until
   * we call Eval's value method to request a result:
   */

  val greeting: Eval[String] = Eval
    .always { println("Step 1"); "Hello" }
    .map { str => println("Step 2"); s"$str world" }

  greeting.value
  // Step 1
  // Step 2
  // Hello world

  /**
   * Note that, while the semantics of the originating `Eval` instances are maintained, mapping functions
   * are always called lazily on demand (def semantics):
   */
  val ans = for {
    a <- Eval.now { println("Calculating A"); 40 }
    b <- Eval.always { println("Calculating B"); 2 }
  } yield {
    println("Adding A and B"); a + b
  }
  // Calculating A

  ans.value // first access
  // Calculating B
  // Adding A and B
  // 42

  ans.value // second access
  // Calculating B
  // Adding A and B
  // 42

  /**
   * Eval has a memoize method that allows us to memoize a chain of computations. The result of the chain up
   * to the call to memoize is cached, whereas calculations after the call retain their original semantics:
   */
  val saying: Eval[String] = Eval
    .always { println("Step 1"); "The cat" }
    .map { str => println("Step 2"); s"$str sat on" }
    .memoize
    .map { str => println("Step 3"); s"$str the mat" }

  saying.value // first access
  // Step 1
  // Step 2
  // Step 3
  // The cat sat on the mat

  saying.value // second access
  // Step 3
  // The cat sat on the mat

  /**
   * 4. Trampolining and `Eval.defer`
   *
   * One useful property of Eval is that its `map` and `flatMap` methods are `trampolined`. This means
   * we can nest calls to `map` and `flatMap` arbitrarily without consuming stack frames. We call this
   * property "stack safety".
   */

  // For example, consider this function for calculating factorials:
  def factorial(n: BigInt): BigInt =
    if (n == 1) n else n * factorial(n - 1)

  // It is relatively easy to make this method stack overflow
  // We can rewrite the method using Eval to make it stack safe:
  def factorialPro(n: BigInt): Eval[BigInt] =
    if (n == 1) Eval.now(n) else factorialPro(n - 1).map(_ * n)

  /**
   * However, that didn't work. This is because we're still making all the recursive calls to factorial
   * before we start working with Eval's map method. We can work around this using `Eval.defer`, which
   * takes an existing instance of Eval and defers its evaluation. The `defer` method is trampolined like
   * `map` and `flatMap`, so we can use it as a quick way to make an existing operation stack safe:
   */
  def factorialProPro(n: BigInt): Eval[BigInt] =
    if (n == 1) Eval.now(n) else Eval.defer(factorialProPro(n - 1).map(_ * n))

  /**
   * Eval is a useful tool to enforce stack safety when working on very large computations and data
   * structures. However, we must bear in mind that trampolining is not free. It avoids consuming
   * stack by creating a chain of function objects on the heap. There are still limits on how deeply
   * we can nest computations, but they are bounded by the size of the heap rather than the stack.
   */
}

object ExerciseSaferFoldingUsingEval {

  // The naive implementation of `foldRight` below is not stack safe. Make it so using `Eval`:

  //  def foldRight[A, B](as: List[A], acc: B)(fn: (A, B) => B): B =
  //    as match {
  //      case head :: tail =>
  //        fn(head, foldRight(tail, acc)(fn))
  //      case Nil =>
  //        acc
  //    }

  import cats.Eval

  def foldRightEval[A, B](as: List[A], acc: Eval[B])(fn: (A, Eval[B]) => Eval[B]): Eval[B] =
    as match {
      case head :: tail =>
        Eval.defer(fn(head, foldRightEval(tail, acc)(fn)))
      case Nil =>
        acc
    }

  def foldRight[A, B](as: List[A], acc: B)(fn: (A, B) => B): B =
    foldRightEval(as, Eval.now(acc)) { (a, b) =>
      b.map(fn(a, _))
    }.value

  def foldRightPro[A, B](xs: List[A], acc: B)(f: (A, B) => B): Eval[B] =
    xs match {
      case hd :: tl =>
        //Eval.defer(foldRightPro(tl, acc)(f).map(b => f(hd, b))) // same as below
        Eval.defer(foldRightPro(tl, acc)(f)).map(b => f(hd, b))
      case Nil =>
        Eval.now(acc)
    }

  foldRight(List.fill(10000)(10), 0)((a, b) => a + b)
  foldRightPro(List.fill(10000)(10), 0)((a, b) => a + b).value
}
