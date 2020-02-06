package com.github.jacobbishopxy.theory.semigroupalAndApplicative

/**
 * Created by jacob on 2/7/2020
 */
object ApplySyntax {

  /**
   * Cats provides a convenient apply syntax that provides a shorthand for methods described above. We
   * import the syntax from `cats.syntax.apply`. Here's an example:
   */

  import cats.instances.option._ // for Semigroupal
  import cats.syntax.apply._ // for tupled and mapN

  /**
   * The tupled methods is implicitly added to the tuple of Options. It uses the `Semigroupal` for Option
   * to zip the values inside the Options, creating a single Option of a tuple:
   */

  (Option(123), Option("abc")).tupled
  // Option[(Int, String)] = Some((123, abc))

  /**
   * We can use the same trick on tuples of up to 22 values. Cats defines a separate tupled method for
   * each arity:
   */

  (Option(123), Option("abc"), Option(true)).tupled
  // Option[(Int, String, Boolean)] = Some((123, abc, true))

  /**
   * In addition to tupled, Cats' apply syntax provides a method called `mapN` that accepts an implicit
   * `Functor` and a function of the correct arity to combine the values:
   */

  case class Cat0(name: String, born: Int, color: String)

  (
    Option("Garfield"),
    Option(1978),
    Option("Orange & black")
    ).mapN(Cat0.apply)
  // Option[Cat] = Some(Cat(Garfield, 1978, Orange & black))

  /**
   * Internally `mapN` uses the `Semigroupal` to extract the values from the `Option` and the `Functor` to
   * apply the values to the function.
   *
   * It's nice to see that this syntax is type checked. If we supply a function that accepts the wrong
   * number or types of parameters, we get a compile error.
   */

  /**
   * Fancy Functors and Apply Syntax
   *
   * Apply syntax also has `contramapN` and `imapN` methods that accept `Contravariant` and `Invariant`
   * functors. For example, we can combine `Monoids` using `Invariant`. Here's an example:
   */

  import cats.Monoid
  import cats.instances.boolean._ // for Monoid
  import cats.instances.int._ // for Monoid
  import cats.instances.list._ // for Monoid
  import cats.instances.string._ // for Monoid
  import cats.syntax.apply._ // for imapN
  import cats.instances.invariant._ // for Semigroupal[Monoid]


  case class Cat(name: String, yearOrBirth: Int, favoriteFoods: List[String])

  val tupleToCat: (String, Int, List[String]) => Cat =
    Cat.apply

  val catToTuple: Cat => (String, Int, List[String]) =
    cat => (cat.name, cat.yearOrBirth, cat.favoriteFoods)

  implicit val catMonoid: Monoid[Cat] = (
    Monoid[String],
    Monoid[Int],
    Monoid[List[String]]
  ).imapN(tupleToCat)(catToTuple)

  import cats.syntax.semigroup._ // for |+|

  val garfield = Cat("Garfield", 1978, List("Lasagne"))
  val heathcliff = Cat("Heathcliff", 1988, List("Junk Food"))

  garfield |+| heathcliff
  // Cat = Cat(GarfieldHeathcliff,3966,List(Lasagne, Junk Food))
}
