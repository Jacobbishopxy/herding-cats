package com.github.jacobbishopxy.theory.foldableAndTraverse

/**
 * Created by jacob on 2/9/2020
 *
 * Foldable and Traverse
 *
 * In this chapter we'll look at two type classes that capture iteration over collections:
 *
 * - `Foldable` abstracts the familiar `foldLeft` and `foldRight` operations;
 * - `Traverse` is a higher-level abstraction that uses `Applicatives` to iterate with less pain than folding.
 *
 * We'll start by looking at `Foldable`, and then examine cases where folding becomes complex and `Traverse`
 * becomes convenient.
 */
object FoldableTypeClass {

  /**
   * Foldable
   *
   * The `Foldable` type class captures the `foldLeft` and `foldRight` methods we're used to in sequences like
   * `Lists`, `Vectors`, and `Streams`. Using `Foldable`, we can write generic folds that work with a variety of
   * sequence types. We can also invent new sequences and plug them into our code. `Foldable` gives us great use
   * cases for `Monoids` and the `Eval` monad.
   */

  /**
   * 1. Folds and Folding
   *
   * Let's start with a quick recap of the general concept of folding. We supply an accumulator value and a binary
   * function to combine it with each item in the sequence:
   */

  def show[A](list: List[A]): String =
    list.foldLeft("nil")((accum, item) => s"$item then $accum")

  show(Nil)
  // String = nil
  show(List(1, 2, 3))
  // String = 3 then 2 then 1 then nil

  /**
   * The `foldLeft` method works recursively down the sequence. Our binary function is called repeatedly for each
   * item, the result of each call becoming the accumulator for the next. When we reach the end of the sequence,
   * the final accumulator becomes our final result.
   *
   * Depending on the operation we're performing, the order in which we fold may be important. Because of this
   * there are two standard variants of fold:
   *
   * a. `foldLeft` traverses from "left" to "right" (start to finish);
   * b. `foldRight` traverses from "right" to "left" (finish to start).
   *
   * `foldLeft` and `foldRight` are equivalent if our binary operation is commutative. For example, we can `sum` a
   * `List[Int]` by folding in either direction, using 0 as our accumulator and addition as our operation:
   */

  List(1, 2, 3).foldLeft(0)(_ + _) // 6
  List(1, 2, 3).foldRight(0)(_ + _) // 6

  /**
   * If we provide a non-commutative operator the order of evaluation makes a difference. For example, if we fold
   * using subtraction, we get different results in each direction:
   */

  List(1, 2, 3).foldLeft(0)(_ - _) // -6
  List(1, 2, 3).foldRight(0)(_ - _) // 2
}

object ExerciseReflectingOnFolds {

  // Try using `foldLeft` and `foldRight` with an empty list as the accumulator and `::` as the binary operator.

  List(1, 2, 3).foldLeft(List.empty[Int])((a, i) => i :: a)
  List(1, 2, 3).foldRight(List.empty[Int])((i, a) => i :: a)

}

object ExerciseScafFoldIngOtherMethods {

  /**
   * `foldLeft` and `foldRight` are very general methods. We can use them to implement many of the other high-level
   * sequence operations we know. Prove this to yourself by implementing substitutes for List's `map`, `flatMap`,
   * `filter`, and `sum` methods in terms of `foldRight`.
   */

  def listMap[A, B](list: List[A])(fn: A => B): List[B] =
    list.foldRight(List.empty[B]) { (item, acc) =>
      fn(item) :: acc
    }

  def listFlatMap[A, B](list: List[A])(fn: A => List[B]): List[B] =
    list.foldRight(List.empty[B]) { (item, acc) =>
      fn(item) ::: acc
    }

  def listFilter[A](list: List[A])(fn: A => Boolean): List[A] =
    list.foldRight(List.empty[A]) { (item, acc) =>
      if (fn(item)) item :: acc else acc
    }

  import cats.Monoid

  def listSum[A](list: List[A])(implicit monoid: Monoid[A]): A =
    list.foldRight(monoid.empty)(monoid.combine)

  import cats.instances.int._ // for Monoid

  listSum(List(1, 2, 3))

}

object FoldableInCats {

  /**
   * Cats' `Foldable` abstracts `foldLeft` and `foldRight` into a type class. Instances of `Foldable` define these
   * two methods and inherit a host of derived methods. Cats provides out-of-the-box instances of `Foldable` for a
   * handful of Scala data types: `List`, `Vector`, `Stream`, and `Option`.
   *
   * We can summon instances as usual using `Foldable.apply` and call their implementations of `foldLeft` directly.
   * Here is an example using `List`:
   */

  import cats.Foldable
  import cats.instances.list._ // for Foldable

  val ints: List[Int] = List(1, 2, 3)

  Foldable[List].foldLeft(ints, 0)(_ + _) // 6

  /**
   * Other sequences like `Vector` and `Stream` work in the same way. Here is an example using `Option`, which is
   * treated like a sequence of zero or one elements:
   */

  import cats.instances.option._ // for Foldable

  val maybeInt: Option[Int] = Option(123)

  Foldable[Option].foldLeft(maybeInt, 10)(_ * _) // 1230

  /**
   * 1. Folding Right
   *
   * `Foldable` defines `foldRight` differently to `foldLeft`, in terms of the `Eval` monad:
   *
   * `def foldRight[A, B](fa: F[A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B]`
   *
   * Using `Eval` means folding is always stack safe, even when the collection's default definition of `foldRight`
   * is not. For example, the default implementation of `foldRight` for `Stream` is not stack safe. The longer
   * the stream, the larger the stack requirements for the `fold`. A sufficiently large stream will trigger a
   * `StackOverflowError`:
   */

  import cats.Eval
  import cats.Foldable

  //  def bigData: Stream[Int] = (1 to 100000).toStream // Stream is deprecated in Scala 2.13, use LazyList instead
  //  bigData.foldRight(0L)(_ + _)

  // Using `Foldable` forces us to use stack safe operations, which fixes the overflow exception:

  //  import cats.instances.stream._ // for Foldable
  //
  //  val eval: Eval[Long] = Foldable[Stream].foldRight(bigData, Eval.now(0L)) {(num, ev) =>
  //    ev.map(_ + num)
  //  }
  //
  //  eval.value // 5000050000

  // Scala 2.13
  def bigData2: LazyList[Int] = (1 to 100000).to(LazyList)

  import cats.instances.lazyList._

  val eval2: Eval[Long] = Foldable[LazyList].foldRight(bigData2, Eval.now(0L)) { (num, ev) =>
    ev.map(_ + num)
  }

  eval2.value


  /**
   * 2. Folding with Monoids
   *
   * `Foldable` provides us with a host of useful methods defined on top of `foldLeft`. Many of these are facsimiles
   * of familiar methods from the standard library: `find`, `exists`, `forall`, `toList`, `isEmpty`, `nonEmpty`, and
   * so on:
   */

  Foldable[Option].nonEmpty(Option(42))
  Foldable[List].find(List(1, 2, 3))(_ % 2 == 0)

  /**
   * In addition to these familiar methods, Cats provides two methods that make use of Monoids:
   *
   * - `combineAll` (and its alias fold) combines all elements in the sequence using their Monoid;
   * - `foldMap` maps a user-supplied function over the sequence and combines the results using a Monoid.
   *
   * For example, we can use `combineAll` to sum over a `List[Int]`:
   */

  import cats.instances.int._ // for Monoid

  Foldable[List].combineAll(List(1, 2, 3)) // 6

  /**
   * Alternatively, we can use `foldMap` to convert each `Int` to a `String` and concatenate them:
   */

  import cats.instances.string._ // for Monoid

  Foldable[List].foldMap(List(1, 2, 3))(_.toString) // 123

  /**
   * Finally, we can compose `Foldable`s to support deep traversal of nested sequences:
   */

  import cats.instances.vector._ // for Monoid

  val ints2 = List(Vector(1, 2, 3), Vector(4, 5, 6))

  Foldable[List].compose(Foldable[Vector]).combineAll(ints2) // 21

  /**
   * 3. Syntax for Foldable
   *
   * Every method in `Foldable` is available in syntax form via `cats.syntax.foldable`. In each case, the first
   * argument to the method on `Foldable` becomes the receiver of the method call:
   */

  import cats.syntax.foldable._ // for combineAll and foldMap

  List(1, 2, 3).combineAll // 6
  List(1, 2, 3).foldMap(_.toString) // 123
}

