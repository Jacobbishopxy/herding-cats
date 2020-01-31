package com.github.jacobbishopxy.herdingCats.day2

/**
 * Created by jacob on 1/30/2020
 *
 * Import guide
 *
 * Cats makes heavy use of implicits. Both as a user and an extender of the library, it will be useful to have
 * general idea on where things are coming from.
 */
object S3 {

  /**
   * Implicits review
   *
   * Let's quickly review Scala's imports and implicits! In Scala, imports are used for two purposes:
   * 1. To include names of values and types into the scope.
   * 2. To include implicits into the scope
   *
   * Implicits are for 4 purposes that I can think of:
   * 1. To provide typeclass instances.
   * 2. To inject methods and operators. (static monkey patching)
   * 3. To declare type constraints.
   * 4. To retrieve type information from compiler.
   *
   * Implicits are selected in the following precedence:
   * 1. Values and converters accessible without prefix via local declaration, imports, outer scope, inheritance,
   * and current package object. Inner scope can shadow values when they are named the same.
   * 2. Implicit scope. Values and converters declared in companion objects and package object of the type,
   * its parts, or super types.
   */

  /**
   * Import cats._
   *
   * Now let's see what gets imported with import cats._
   *    First, the names. Typeclasses like `show[A]` and `Functor[F[_]]` are implemented as trait, and are
   * defined under the cats package. So instead of writing `cats.Show[A]` we can write `Show[A]`.
   *    Next, also the names, but type aliases. cats's package object declares type aliases like `Eq[A]` and
   * `~> [F[_], G[_]]`. Again, these can also be accessed as `cats.Eq[A]` if you want.
   *    Finally, `catsInstancesForId` is defined as typeclass instance of `Id[A]` for `Traverse[F[_]]`,
   * `Monad[F[_]]` etc, but it's not relevant. By virtue of declaring an instance within its package object it
   * will be available, so importing doesn't add much.
   */
  //cats.Functor[cats.Id] // No import needed.

  /**
   * Import cats.data._
   *
   * Next let's see what gets imported with import cats.data._
   *    First, more names. There are custom datatype defined under the cats.data package such as
   * `Validated[+E, +A]`.
   *    Next, the type aliases. The `cats.data` package object defines type aliases such as `Reader[A, B]`,
   * which is treated as a specialization of `ReaderT` transformer.
   */

  /**
   * Import cats.implicits._
   *
   * Here is the definition of `implicits` object:
   * `
   * package cats
   *
   * object implicits extends syntax.AllSyntax with instances.AllInstances
   * `
   * This is quite a nice way of organizing the imports. `implicits` object itself doesn't define anything and
   * it just mixes in the traits.
   */

  /**
   * a la carte style
   *
   * If for whatever reason if you do not wish to import the entire `cats.implicits._`, you can pick and choose.
   *
   * 1. typeclass instances
   * Typeclass instances are broken down by the datatypes. Here's how to get all typeclass instances for Option:
   * `
   * import cats.instances.option._
   * cats.Monad[Option].pure(0)
   * `
   * If you just want all instances, here's how to load them all:
   * `
   * import cats.instances.all._
   * cats.Monoid[Int].empty
   * `
   * Because we have not injected any operators, you would have to work more with helper functions and functions
   * under typeclass instances, which could be exactly what you want.
   *
   * 2. Cats typeclass syntax
   * Typeclass syntax are broken down by the typeclass. Here's how to get injected methods and operators for `Eq`s:
   * `
   * import cats.syntax.eq._
   * import cats.instances.all._
   * 1 === 1
   * `
   *
   * 3. Cats datatype syntax
   * Cats datatype syntax like `Write` are also available under `cats.syntax` package:
   * `
   * import cats.syntax.writer._
   * import cats.instances.all._
   * 1.tell
   * `
   *
   * 4. standard datatype syntax
   * Standard datatype syntax are broken down by the datatypes. Here's how to get injected methods and functions
   * for `Option`:
   * `
   * import cats.syntax.option._
   * import cats.instances.all._
   * 1.some
   * `
   *
   * 5. all syntax
   * Here's how to load all syntax and all instances.
   * `
   * import cats.syntax.all._
   * import cats.instances.all._
   * 1.some
   * `
   * This is the same as importing `cats.implicits._`.
   */
}
