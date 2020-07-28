package com.github.jacobbishopxy.caseStudies.dataValidation

/**
 * Created by Jacob on 2/18/2020
 *
 * In this case study we will build a library for validation. What do we mean by validation? Almost all programs
 * must check their input meets certain criteria. User names must not be blank, email addresses must be valid,
 * and so on. This type of validation often occurs in web forms, but it could be performed on configuration files,
 * on web service responses, and any other case where we have to deal with data that we can't guarantee is correct.
 * Authentication, for example, is just a specialised form of validation.
 *
 * We want to build a library that performs these checks. What design goals should we have? For inspiration, let's
 * look at some examples of the types of checks we want to perform:
 *
 * - A user must be over 18 years old or must have parental consent.
 * - A String ID must be parsable as a Int and the Int must correspond to a valid record ID.
 * - A bid in an auction must apply to one or more items and have a positive value.
 * - A username must contain at least four characters and all characters must be alphanumeric.
 * - An email address must contain a single @ sign. Split the string at the @. The string to the left must not be
 * empty. The string to the right must be at least three characters long and contain a dot.
 *
 * With these examples in mind we can state some goals:
 *
 * - We should be able to associate meaningful messages with each validation failure, so the user knows why their
 * data is not valid.
 * - We should be able to combine small checks into larger ones. Taking the username example above, we should be
 * able to express this by combining a check of length and a check for alphanumeric values.
 * - We should be able to transform data while we are checking it. There is an example above requiring we parse
 * data, changing its type from String to Int.
 * - Finally, we should be able to accumulate all the failures in one go, so the user can correct all the issues
 * before resubmitting.
 *
 * These goals assume we're checking a single piece of data. We will also need to combine checks across multiple
 * pieces of data. For a login form, for example, we'll need to combine the check results for the username and
 * the password. This will turn out to be quite a small component of the library, so the majority of our time will
 * focus on checking a single data item.
 */
object SketchingTheLibraryStructure {

  /**
   * Sketching the Library Structure
   *
   * Let's start at the bottom, checking individual pieces of data. Before we start coding let's try to develop a
   * feel for what we'll be building. We can use a graphical notation to help us. We'll go through our goals one by
   * one.
   *
   * a. Providing error messages
   *
   * Our first goal requires us to associate useful error messages with a check failure. The output of a check could
   * be either the value being checked, if it passed the check, or some kind of error message. We can abstractly
   * represent this as a value in a context, where the context is the possibility of an error message. A check itself
   * is therefore a function that transforms a value into a value in a context.
   *
   * b. Combine checks
   *
   * How do we combine smaller checks into larger ones? Is this an applicative or semigroupal?
   * Not really. With applicative combination, both checks are applied to the same value and result in a tuple with
   * the value repeated. What we want feels more like a monoid. We can define a sensible identity -- a check a that
   * always passes -- and two binary combination operators -- `and` and `or`;
   * We'll probably be using `and` and `or` about equally often with our validation library and it will be annoying
   * to continuously switch between two monoids for combining rules. We consequently won't actually use the monoid
   * API: we'll use two separate methods, `and` and `or`, instead.
   *
   * c. Accumulating errors as we check
   *
   * Monoids also feel like a good mechanism for accumulating error messages. If we store messages as a `List` or
   * `NonEmptyList`, we can even use a preexisting monoid from inside Cats.
   *
   * d. Transforming data as we check it
   *
   * In addition to checking data, we also have the goal of transforming it. This seems like it should be a `map` or
   * a `flatMap` depending on whether the transform can fail or not.
   */
}

