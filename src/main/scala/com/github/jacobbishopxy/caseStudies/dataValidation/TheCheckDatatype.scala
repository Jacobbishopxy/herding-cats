package com.github.jacobbishopxy.caseStudies.dataValidation

/**
 * Created by Administrator on 2/18/2020
 */
object TheCheckDatatype {

  /**
   * Our design revolves around a Check, which we said was a function from a value to a value in a context. As soon
   * as you see this description you should think of something like:
   *
   * `type Check[A] = A => Either[String, A]`
   *
   * Here we've represented the error message as a String. This is probably not the best representation. We may want
   * to accumulate messages in a List, for example, or even use a different representation that allows for
   * internationalization or standard error codes.
   *
   * We could attempt to build some kind of `ErrorMessage` type that holds all the information we can think of.
   * However, we can't predict the user's requirements. Instead let's let the user specify what they want. We can do
   * this by adding a second type parameter to Check:
   *
   * `type Check[E, A] => A => Either[E, A]`
   *
   * We will probably want to add custom methods to Check so let's declare it as a trait instead of a type alias:
   *
   * `
   * trait Check[E, A] {
   * def apply(value: A): Either[E, A]
   * }
   * `
   *
   * There are two functional programming patterns that we should consider when defining a trait:
   * - we can make it a typeclass, or;
   * - we can make it an algebraic data type (and hence seal it).
   *
   * Type classes allow us to unify disparate data types with a common interface. This doesn't seem like what we're
   * trying to do here. That leaves us with an algebraic data type.
   */
}
