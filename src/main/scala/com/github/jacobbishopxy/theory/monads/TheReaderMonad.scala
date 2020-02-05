package com.github.jacobbishopxy.theory.monads

/**
 * Created by jacob on 2/4/2020
 *
 * `cats.data.Reader` is a monad that allows us to sequence operations that depend on some input.
 * Instances of Reader wrap up functions of one argument, providing us with useful methods for
 * composing them.
 *
 * One common use for Readers is dependency injection. If we have a number of operations that all
 * depend on some external configuration, we can chain them together using a Reader to produce
 * one large operation that accepts the configuration as a parameter and runs our program in the
 * order specified.
 */
object TheReaderMonad {

  /**
   * 1. Creating and Unpacking Readers
   *
   * We can create a `Reader[A, B]` from a function A =>B using the `Reader.apply` constructor:
   */

  import cats.data.Reader

  case class Cat(name: String, favoriteFood: String)

  val catName: Reader[Cat, String] = Reader(cat => cat.name)

  // We can extract the function again using the Reader's run method and call it using apply as usual:
  catName.run(Cat("Garfield", "lasagne")) // cats.Id[String] = Garfield

  /**
   * 2. Composing Readers
   *
   * The power of Readers comes from their `map` and `flatMap` methods, which represent different kinds
   * of function composition. We typically create a set of Readers that accept the same type of
   * configuration, combine them with `map` and `flatMap`, and then cal run to inject the config at the
   * end.
   *
   * The `map` method simply extends the computation in the Reader by passing its result through a
   * function:
   */

  val greetKitty: Reader[Cat, String] = catName.map(name => s"Hello $name")

  greetKitty.run(Cat("Heathcliff", "junk food")) // cats.Id[String] = Hello Heathcliff

  /**
   * The `flatMap` method is more interesting. It allows us to combine readers that depend on the same
   * input type. To illustrate this, let's extend our greeting example to also feed the cat"
   */
  val feedKitty: Reader[Cat, String] =
    Reader(cat => s"Have a nice bowl of ${cat.favoriteFood}")

  val greetAndFeed: Reader[Cat, String] =
    for {
      greet <- greetKitty
      feed <- feedKitty
    } yield s"$greet. $feed."

  greetAndFeed(Cat("Garfield", "lasagne"))
  // cats.Id[String] = Hello Garfield. Have a nice bowl of lasagne.

  greetAndFeed(Cat("Heathcliff", "junk food"))
  // cats.Id[String] = Hello Heathcliff. Have a nice bowl of junk food.

  /**
   * When to Use Readers?
   *
   * Readers provide a tool for doing dependency injection. We write steps of our program as instances
   * of Reader, chain them together with `map` and `flatMap`, and build a function that accepts the
   * dependency as input.
   *
   * There are many ways of implementing dependency injection in Scala, from simple techniques like
   * methods with multiple parameter lists, through implicit parameter and type classes, to complex
   * techniques like the cake pattern and DI frameworks.
   *
   * Readers are most useful in situations where:
   *
   * a. we are constructing a batch program that can easily be represented by a function;
   * b. we need to defer injection of a known parameter or set of parameters;
   * c. we want to be able to test parts of the program in isolation.
   *
   * By representing the steps of our program as Readers we can test them as easily as pure functions,
   * plus we gain access to the `map` and `flatMap` combinators.
   *
   * For more advanced problems where we have lots of dependencies, or where a program isn't easily
   * represented as a pure function, other dependency injection techniques tends to be more appropriate.
   */
}

object ExerciseTheReaderMonad {

  // Hacking on Readers

}
