package com.github.jacobbishopxy.theory.introduction

/**
 * Created by jacob on 2/8/2020
 *
 * Working with type classes in Scala means working with implicit values and implicit parameters. There
 * are few rules we need to know to do this effectively.
 */
object WorkingWithImplicits {

  /**
   * 1. Packaging Implicits
   *
   * In a curious quirk of the language, any definitions marked `implicit` in Scala must be placed inside
   * an object or trait rather than at the top level. In the example above we packaged our type class
   * instances in an object called `JsonWriterInstances`. We could equally have placed them in a companion
   * object to `JsonWriter`. Placing instances in a companion object to the type class has special
   * significance in Scala because it plays into something called `implicit scope`.
   */

  /**
   * 2. Implicit Scope
   *
   * As we saw above, the compiler searches for candidate type class instances by type. For example, in the
   * following expression it will look for an instance of type `JsonWriter[String]`:
   *
   * `Json.toJson("A string!")`
   *
   * The compiler searches for candidate instances in the `implicit scope` at the call site, which roughly
   * consists of:
   *
   * - local or inherited definitions;
   * - imported definitions;
   * - definitions in the companion object of the type class or the parameter type (in this case `JsonWriter`
   * or `String`).
   *
   * Definitions are only included in implicit scope if they are tagged with the `implicit` keyword.
   * Furthermore, if the compiler sees multiple candidate definitions, it fails with an ambiguous implicit
   * values error.
   *
   * The precise rules of implicit resolution are more complex than this, but the complexity is largely
   * irrelevant for this book. For our purposes, we can package type class instances in roughly four ways:
   *
   * a. by placing them in an object such as `JsonWriterInstances`;
   * b. by placing them in a trait;
   * c. by placing them in the companion object of the type class;
   * d. by placing them in the companion object of the parameter type.
   *
   * With option 1 we bring instances into scope by importing them. With option 2 we bring them into scope
   * with inheritance. With options 3 and 4, instances are always in implicit scope, regardless of where we
   * try to use them.
   */

  /**
   * 3. Recursive Implicit Resolution
   *
   * The power of type classes and implicits list in the compiler's ability to combine implicit definitions
   * when searching for candidate instances.
   *
   * Earlier we insinuated that all type class instances are `implicit vals`. This was a simplification. We
   * can actually define instances in two ways:
   * a. by defining concrete instances as `implicit vals` of the required type;
   * b. by defining implicit methods to construct instances from other type class instances.
   *
   * Why would we construct instances from other instances? As a motivational example, consider defining a
   * `JsonWriter` for Options. We would need a `JsonWriter[Option[A]]` for every `A` we care about in our
   * application. We could try to brute force the problem by creating a library of `implicit vals`:
   *
   *
   * `
   * implicit val optionIntWriter: JsonWriter[Option[Int]] = ???
   * implicit val optionPersonWriter: JsonWriter[Option[Person]] = ???
   * // and so on...
   * `
   *
   * However, this approach clearly doesn't scale. We end up requiring two `implicit vals` for every
   * type `A` in our application: one for `A` and one for `Option[A]`.
   *
   * Fortunately, we can abstract the code for handling `Option[A]` into a common constructor based on the
   * instance for A:
   * a. If the option is Some(aValue), write aValue using the writer for `A`;
   * b. If the option is None, write null.
   *
   * Here is the same code written out as an `implicit def`:
   */

  import com.github.jacobbishopxy.theory.introduction.AnatomyOfATypeClass.{JsonWriter, JsNull}

  implicit def optionWriter[A](implicit writer: JsonWriter[A]): JsonWriter[Option[A]] =
    new JsonWriter[Option[A]] {
      override def write(value: Option[A]): AnatomyOfATypeClass.Json =
        value match {
          case Some(aValue) => writer.write(aValue)
          case None => JsNull
        }
    }

  /**
   * This method constructs a `JsonWriter` for `Option[A]` by relying on an implicit parameter to fill in
   * the A-specific functionality. When the compiler sees an expression like this:
   *
   * `Json.toJson("A string")`
   *
   * It searches for an implicit `JsonWriter[Option[String]]`. It finds the implicit method for
   * `JsonWriter[Option[A]]`:
   *
   * `Json.toJson(Option("A string"))(optionWriter[String])`
   *
   * and recursively searches for a `JsonWriter[String]` to use as the parameter to `optionWriter`:
   *
   * `Json.toJson(Option("A string"))(optionWriter(stringWriter))`
   *
   * In this way, implicit resolution becomes a search through the space of possible combinations of
   * implicit definitions, to find a combination that summons a type class instance of the correct overall
   * type.
   */
}
