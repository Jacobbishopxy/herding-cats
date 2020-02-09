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
object Foldable {

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

}

object ExerciseScafFoldIngOtherMethods {

}
