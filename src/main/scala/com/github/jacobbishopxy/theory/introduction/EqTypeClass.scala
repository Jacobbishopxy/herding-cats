package com.github.jacobbishopxy.theory.introduction

/**
 * Created by jacob on 2/9/2020
 *
 * We will finish off this chapter by looking at another useful type class: `cats.Eq`. `Eq` is designed
 * to support type-safe equality and address annoyances using Scala's built-in `==` operator.
 *
 * Almost every Scala developer has written code like this before:
 * `List(1,2,3).map(Option(_)).filter(item => item == 1)`
 *
 * Ok, many of you won't have made such a simple mistake as this, but the principle is sound. The predicate
 * in the `filter` clause always returns `false` because it is comparing an `Int` to an `Option[Int]`.
 *
 * This is programmer error -- we should have compared item to `Some(1)` instead of 1. However, it's not
 * technically a type error because `==` works for any pair of objects, no matter what types we compare.
 * `Eq` is designed to add some type safety to equality checks and work around this problem.
 */
object EqTypeClass {

  /**
   * 1. Equality, Liberty, and Fraternity
   *
   * We can use `Eq` to define type-safe equality between instances of any given type:
   *
   * `
   * package cats
   *
   * trait Eq[A] {
   * def eqv(a: A, b: A): Boolean
   * // other concrete methods based on eqv...
   * }
   * `
   *
   * The interface syntax, define in `cats.syntax.eq`, provides two methods for performing equality checks
   * provided there is an instance `Eq[A]` in scope:
   *
   * - `===` compares two objects for equality;
   * - `=!=` compares two objects for inequality.
   */

  /**
   * 2. Comparing Ints
   *
   * Let's look at a few examples. First we import the type class:
   */

  import cats.Eq

  // Now let's grab an instance for `Int`:

  import cats.instances.int._ // for Eq

  val eqInt: Eq[Int] = Eq[Int]

  // We can use eqInt directly to test for equality:
  eqInt.eqv(123, 123)

  eqInt.eqv(123, 234)

  // Unlike Scala's `==` method, if we try to compare objects of different types using eqv we get a compile
  // error:
  // eqInt.eqv(123, "234")

  // We can also import the interface syntax in `cats.syntax.eq` to use the `===` and `=!=` methods:

  import cats.syntax.eq._ // for === and =!=

  123 === 123
  123 =!= 234

  // Again, comparing values of different types causes a compiler error:
  // 123 === "123"

  /**
   * 3. Comparing Options
   *
   * Now for a more interesting example -- `Option[Int]`. To compare values of type `Option[Int]` we need to
   * import instances of `Eq` for `Option` as well as Int:
   */

  import cats.instances.int._ // for Eq
  import cats.instances.option._ // for Eq

  // Now we can try some comparisons:
  // Some(1) === None

  /**
   * We have received an error here because the types don't quite match up. We have `Eq` instances in scope
   * for `Int` and `Option[Int]` but the values we are comparing are of type `Some[Int]`. To fix the issue we
   * have to re-type the arguments as `Option[Int]`:
   */

  (Some(1): Option[Int]) === (None: Option[Int])

  // We can do this in a friendlier fashion using the `Option.apply` and `Option.empty` methods from the
  // standard library:

  Option(1) === Option.empty[Int]

  // or using special syntax from `cats.syntax.option`:

  import cats.syntax.option._ // for some and none

  1.some === none[Int]

  1.some =!= none[Int]

  /**
   * 4. Comparing Custom Types
   *
   * We can define our own instances of `Eq` using the `Eq.instance` method, which accepts a function of type
   * `(A, A) => Boolean` and returns an `Eq[A]`:
   */

  import java.util.Date
  import cats.instances.long._ // for Eq

  implicit val dateEq: Eq[Date] = Eq.instance[Date] {(date1, date2) =>
    date1.getTime === date2.getTime
  }

  val x = new Date()
  val y = new Date()

  x === y
  x =!= y
}

object ExerciseEqualityLibertyAndFelinity {

  // Implement an instance of `Eq` for our running Cat example:
  final case class Cat(name: String, age: Int, color: String)

  // Use this to compare the following pairs of objects for equality and inequality:
  val cat1: Cat = Cat("Garfield", 38, "orange and black")
  val cat2: Cat = Cat("Heathcliff", 33, "orange and black")
  val optionCat1: Option[Cat] = Option(cat1)
  val optionCat2: Option[Cat] = Option.empty

  import cats.Eq
  import cats.instances.string._
  import cats.instances.int._
  import cats.instances.option._
  import cats.syntax.eq._

  implicit val catEq: Eq[Cat] = Eq.instance[Cat] {(cat1, cat2) =>
    val cond1 = cat1.name === cat2.name
    val cond2 = cat1.age === cat2.age
    val cond3 = cat1.color === cat2.color
    cond1 && cond2 && cond3
  }

  cat1 === cat2
  cat1 =!= cat2

  optionCat1 === optionCat2
  optionCat1 =!= optionCat2
}
