package com.github.jacobbishopxy.theory.introduction

/**
 * Created by jacob on 2/8/2020
 *
 * Scala provides a `toString` method to let us convert any value to a `String`. However, this method
 * comes with a few disadvantages: it is implemented for every type in the language, many implementations
 * are of limited use, and we can't opt-in to specific implementations for specific types.
 */
object PrintableLibrary {

  /**
   * Let's define a `Printable` type class to work around these problems:
   *
   * a. Define a type class `Printable[A]` containing a single method `format`. `format` should accept a
   * value of type `A` and return a `String`.
   * b. Create an object `PrintableInstances` containing instances of `Printable` for `String` and `Int`.
   * c. Define an object `Printable` with two generic interface methods:
   *    - `format` accepts a value of type `A` and a `Printable` of corresponding type. It uses the relevant
   * `Printable` to convert the `A` to a `String`
   *    - `print` accepts the same parameters as `format` and returns `Unit`. It prints the `A` value to the
   * console using `println`.
   */

  trait Printable[A] {
    def format(value: A): String
  }

  object PrintableInstances {
    implicit val printableString: Printable[String] =
      (value: String) => value
    implicit val printableInt: Printable[Int] =
      (value: Int) => value.toString
  }

  object Printable {
    def format[A](value: A)(implicit printable: Printable[A]): String =
      printable.format(value)

    def print[A](value: A)(implicit printable: Printable[A]): Unit =
      println(format(value))
  }

  /**
   * Using the Library
   *
   * The code above forms a general purpose printing library that we can use in multiple applications. Let's
   * define an "application" now that uses the library.
   *
   * First we'll define a data type to represent a well-known type of furry animal:
   */

  final case class Cat(name: String, age: Int, color: String)

  /**
   * Next we'll create an implementation of `Printable` for Cat that returns content in the following format:
   *
   * NAME is a AGE year-old COLOR cat.
   *
   * Finally, use the type class on the console or in a short demo app: create a Cat and print it to the
   * console:
   *
   * `
   * // define a cat:
   * val cat = Cat(...)
   * // Print the cat!
   * `
   */

  import PrintableInstances._

  implicit val catPrintable: Printable[Cat] = (value: Cat) => {
    val name = Printable.format(value.name)
    val age = Printable.format(value.age)
    val color = Printable.format(value.color)
    s"$name is a $age year-old $color cat."
  }

  val cat: Cat = Cat("Garfield", 38, "ginger and black")

  Printable.print(cat)

  /**
   * Better Syntax
   *
   * Let's make our printing easier to use by defining some extension methods to provide better syntax:
   *
   * a. Create an object called `PrintableSyntax`.
   * b. Inside `PrintableSyntax` define an `implicit class` `PrintableOps[A]` to wrap up a value of type `A`.
   * c. In `PrintableOps` define the following methods:
   *    - `format` accepts an implicit `Printable[A]` and returns a `String` representation of the wrapped `A`.
   *    - `print` accepts an implicit `Printable[A]` and returns a `Unit`. It prints the wrapped `A` to the console.
   * d. Use the extension methods to print the example Cat you created in the previous exercise.
   */

  object PrintableSyntax {
    implicit class PrintableOps[A](value: A) {
      def format(implicit printable: Printable[A]): String = printable.format(value)
      def print(implicit printable: Printable[A]): Unit = println(printable.format(value))
    }
  }

  import PrintableSyntax._

  cat.print
}
