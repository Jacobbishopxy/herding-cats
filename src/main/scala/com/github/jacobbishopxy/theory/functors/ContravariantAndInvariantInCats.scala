package com.github.jacobbishopxy.theory.functors

/**
 * Created by jacob on 2/3/2020
 */
object ContravariantAndInvariantInCats {

  /**
   * Contravariant in Cats
   *
   * We can summon instances of Contravariant using the `Contravariant.apply` method. Cats provides
   * instances for data types that consume parameters, including `Eq`, `Show` and `Function1`.
   * Here's an example:
   */

  import cats.Contravariant
  import cats.Show
  import cats.instances.string._

  val showString = Show[String]

  val showSymbol = Contravariant[Show]
    .contramap(showString)((sym: Symbol) => s"'${sym.name}")

  showSymbol.show(Symbol("jacob"))

  // More conveniently, we can use `cats.syntax.contravariant`, which provides a contramap extension method:

  import cats.syntax.contravariant._

  showString.contramap[Symbol](_.name).show(Symbol("jacob"))

  /**
   * Invariant in Cats
   *
   * Among other types, Cats provides an instance of Invariant for Monoid.
   *
   * Imagine we want to produce a Monoid for Scala's Symbol type. Cats doesn't provide a Monoid for Symbol
   * but it does provide a Monoid for a similar type: String. We can write our new semigroup with an empty
   * method that relies on the empty String, and a combine method that works as follows:
   * 1. accept two Symbols as parameters;
   * 2. convert the Symbols to Strings;
   * 3. combine the Strings using Monoid[String];
   * 4. convert the result back to a Symbol.
   *
   * We can implement combine using imap, passing functions of type String => Symbol and Symbol => String
   * as parameters. Here's the code, written out using the imap extension method provided by
   * `cats.syntax.invariant`:
   */

  import cats.Monoid
  import cats.instances.string._ // for Monoid
  import cats.syntax.invariant._ // for imap
  import cats.syntax.semigroup._ // for |+|

  implicit val symbolMonoid: Monoid[Symbol] =
    Monoid[String].imap(Symbol.apply)(_.name)

  Monoid[Symbol].empty // Symbol("")
  Symbol("a") |+| Symbol("few") |+| Symbol("words") // Symbol("afewwords")

}

