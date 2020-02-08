package com.github.jacobbishopxy.theory.introduction

/**
 * Created by jacob on 2/8/2020
 *
 * In the previous section we saw how to implement type classes in Scala. In this section we will look at
 * how type classes are implemented in Cats.
 *
 * Cats is written using a modular structure that allows us to choose which type classes, instances, and
 * interface methods we want to use. Let's take a first look using `cats.Show` as an example.
 *
 * `Show` is Cat's equivalent of `Printable` type class we defined in the last section. It provides a
 * mechanism for producing developer-friendly console output without using `toString`. Here's an abbreviated
 * definition:
 *
 * `
 * package cats
 *
 * trait Show[A] {
 * def show(value: A): String
 * }
 * `
 */
object MeetCats {

  /**
   * 1. Importing Type Classes
   *
   * The type classes in Cats are defined in the `cats` package. We can import `Show` directly from this
   * package:
   *
   * `import cats.Show`
   *
   * The companion object of every Cats type class has an `apply` method that locates an instance for any
   * type we specify:
   *
   * `val showInt = Show.apply[Int]`
   *
   * Oops -- that didn't work! the `apply` method uses implicits to look up individual instances, so we'll
   * have to bring some instances into scope.
   */

  /**
   * 2. Importing Default Instances
   *
   * The `cats.instances` package provides default instances for a wide variety of types. We can import these
   * as shown in the table below. Each import provides instances of all Cats' type classes for a specific
   * parameter type:
   *
   * - `cats.instances.int` provides instances for Int
   * - `cats.instances.string` provides instances for String
   * - `cats.instances.list` provides instances for List
   * - `cats.instances.option` provides instances for Option
   * - `cats.instances.all` provides all instances that are shipped out of the box with Cats
   *
   * See the `cats.instances` package for a complete list of available imports.
   * Let's import the instances of `Show` for Int and String:
   */

  import cats.Show

  import cats.instances.int._ // for Show
  import cats.instances.string._ // for Show

  val showInt: Show[Int] = Show.apply[Int]
  val showString: Show[String] = Show.apply[String]

  /**
   * That's better! We now have access to two instances of `Show`, and can use them to print `Ints` and
   * `Strings`:
   */

  val intAsString: String = showInt.show(123)
  val stringAsString: String = showString.show("abc")

  /**
   * 3. Importing Interface Syntax
   *
   * We can make `Show` easier to use by importing the `interface syntax` from `cats.syntax.show`. This
   * adds an extension method called show to any type for which we have an instance of `Show` in scope:
   */

  import cats.syntax.show._ // for show

  val shownInt = 123.show
  // shownInt: String = 123
  val shownString = "abc".show
  // shownString: String = abc

  /**
   * 4. Importing All The Things!
   *
   * In this book we will use specific imports to show you exactly which instances and syntax you need in
   * each example. However, this can be time consuming for many use cases. You should feel free to take
   * one of the following shortcuts to simplify your imports:
   *
   * - `import cats._`: import of Cats' type classes in one go;
   * - `import cats.instances.all._`: imports all of the type class instances for the standard library in
   * one go;
   * - `import cats.syntax.all._`: imports all of the syntax in one go;
   * - `import cats.implicits._`: imports all of the standard type class instances and all of the syntax
   * in one go.
   *
   * Most people start their files with the following imports, reverting to more specific imports only if
   * they encounter naming conflicts or problems ambiguous implicits:
   *
   * `
   * import cats._
   * import cats.implicits._
   * `
   */

  /**
   * 5. Defining Custom Instances
   *
   * We can define an instance of `Show` simply by implementing the trait for a given type:
   */

  import java.util.Date

  implicit val dateShow: Show[Date] =
    (date: Date) => s"${date.getTime}ms since the epoch."

  /**
   * However, Cats also provides a couple of convenient methods to simplify the process. There are two
   * construction methods on the companion object of `Show` that we can use to define instances for our
   * own types:
   */

  object MyShow {
    // Convert a function to a `Show` instance:
    def show[A](f: A => String): Show[A] = ???

    // Create a `Show` instance from a `toString` method:
    def fromToString[A]: Show[A] = ???
  }

  // These allows us to quick construct instances with less ceremony than defining them from scratch:
  implicit val dateShow2: Show[Date] =
    Show.show(date => s"${date.getTime}ms since the epoch.")

  /**
   * As you can see, the code using construction methods is much terser than the code without. Many
   * type classes in Cats provide helper methods like these for constructing instances, either from
   * scratch or by transforming existing instances for other types.
   */
}

object ExerciseCatShow {

  // Cat Show
  // Re-implement the Cat application from the previous section using `Show` instead of `Printable`.

  import cats.Show
  import cats.instances.int._
  import cats.instances.string._
  import cats.syntax.show._

  final case class Cat(name: String, age: Int, color: String)

  implicit val catShow: Show[Cat] = Show.show[Cat] { cat =>
    val name = cat.name.show
    val age = cat.age.show
    val color = cat.color.show
    s"$name is a $age year-old $color cat."
  }

  Cat("Garfield", 38, "ginger and black").show
}
