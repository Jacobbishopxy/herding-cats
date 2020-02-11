# Theory

## Introduction

1. [Anatomy of a Type Class](introduction/AnatomyOfATypeClass.scala)

2. [Working with Implicits](introduction/WorkingWithImplicits.scala)

3. [Exercise: Printable Library](introduction/PrintableLibrary.scala)

4. [Meet Cats](introduction/MeetCats.scala)

5. [Example: Eq](introduction/EqTypeClass.scala)

6. [Controlling Instance Selection](introduction/ControllingInstanceSelection.scala)

### Summary

In this chapter we took a first look at type classes. We implemented our own `Printable` type class using 
plain Scala before looking at two examples from Cats -- `Show` and `Eq`.

We have now seen the general patterns in Cats type classes:

- The type classes themselves are general patterns in Cats type classes;

- Each type class has a companion object, an `apply` method for materializing instances, one or more 
construction methods for creating instances, and a collection of other relevant helper methods.

- Default instances are provided via objects in the `cats.instances` package, and are organized by parameter
type rather than by type class.

- Many type classes have syntax provided via the `cats.syntax` package.

In the remaining chapters of Part I we will look at several broad and powerful type classes -- `Semigroup`, 
`Monoid`, `Functor`, `Monad`, `Semigroupal`, `Applicative`, `Traverse`, and more. In each case we will learn
what functionality the type class provides, the formal rules it follows, and how it is implemented in Cats.
Many of these type classes are more abstract than `Show` or `Eq`. While this makes them harder to learn, it 
makes them far more useful for solving general problems in our code.

## Monoids and Semigroups

1. [Definitions](monoidsAndSemigroups/Definitions.scala)

2. [Monoids in Cats](monoidsAndSemigroups/MonoidsInCats.scala)

### Summary

We hit a big milestone in this chapter -- we covered our first type classes with fancy function programming
names:

- a `Semigroup` represents an addition or combination operation;

- a `Monoid` extends a `Semigroup` by adding an identity or "zero" element.

We can use `Semigroups` and `Monoids` by importing three things: the type classes themselves, the instances 
for the types we care about, and the semigroup syntax to give us the `|+|` operator.

We can also write generic code that works with any type for which we have an instance of Monoid:
```
def addAll[A](values: List[A])
             (implicit monoid: Monoid[A]): A =
    values.foldRight(monoid.empty)(_ |+| _)

add(List(1, 2, 3)
add(List(None, Some(1), Some(2)))
```

Monoids are a great gateway to Cats. They're easy to understand and simple to use. However, they're just
the tip of the iceberg in terms of the abstractions Cats enables us to make. In the next chapter we'll look at
`functors`, the type class personification of the beloved map method.

## Functors

1. [Definition](functors/Definition.scala)

2. [Functors in Cats](functors/FunctorsInCats.scala)

### Summary

Functors represent sequencing behaviours. We covered three types of functor in this chapter:

- Regular covariant Functors, with their `map` method, represent the ability to apply functions to a value
in some context. Successive calls to `map` apply these functions in sequence, each accepting the result of 
its predecessor as a parameter.

- Contravariant functors, with their `contramap` method, represent the ability to "prepend" functions to a
function-like context. Successive calls to `contramap` sequence these functions in the opposite order to
`map`.

- Invariant functors, with their `imap` method, represent bidirectional transformations.

Regular Functors are by far the most common of these type classes, but even then it is rare to use them on
their own. Functors form a foundational building block of several more interesting abstractions that we use
all the time. In the following chapters we will look at two of these abstractions: `monads` and `applicative`
functors.

Functors for collections are extremely important, as they transform each element independently of the rest.
This allows us to parallelize or distribute transformations on large collections, a technique leverage 
heavily in "map-reduce" frameworks like Hadoop. We will investigate this approach in more detail in the
Map-reduce case study later in the book.

The Contravariant and Invariant type classes are less widely applicable but are still useful for building
data types that represent transformations. We will revisit them to discuss the `Semigroupal` type class later
in Chapter 6.

## Monads

1. [Definition](monads/Definition.scala)

2. [Mondas in Cats](monads/MonadsInCats.scala)

3. [The Identity Monad](monads/TheIdentityMonad.scala)

4. [Either](monads/EitherMonad.scala)

5. [Error Handling and MonadError](monads/ErrorHandlingAndMonadError.scala)

6. [The Eval Monad](monads/TheEvalMonad.scala)

7. [The Writer Monad](monads/TheWriterMonad.scala)

8. [The Reader Monad](monads/TheReaderMonad.scala)

9. [The State Monad](monads/TheStateMonad.scala)

10. [Defining Custom Monads](monads/DefiningCustomMonads.scala)

### Summary

In this chapter we've seen monads up-close. We saw that `flatMap` can be viewed as an operator for sequencing
computations, dictating the order in which operations must happen. From this viewpoint, `Option` represents
a computation that can fail without an error message, `Either` represents computations that can fail with a 
message, `List` represents multiple possible results, and `Future` represents a computation that may produce
a value at some point in the future.

We've also seen some of the custom types and data structures that Cats provides, including `Id`, `Reader`, 
`Writer`, and `State`. These cover a wide range of use cases.

Finally, in the unlikely event that we have to implement a custom monad, we've learned about defining our 
own instance using `tailRecM`. `tailRecM` is an odd wrinkle that is a concession to building a functional
programming library that is stack-safe by default. We don't need to understand `tailRecM` to understand 
monads, but having it around gives us benefits of which we can grateful when writing monadic code.

## Monad Transformers

1. [Composing Monads](monadTransformers/ComposingMonads.scala)

2. [Monad Transformers in Cats](monadTransformers/MonadTransformersInCats.scala)

3. [Exercise: Monads: Transform and Roll Out](monadTransformers/TransformAndRollOut.scala)

### Summary

In this chapter we introduced monad transformers, which eliminate the need for nested for comprehensions
and pattern matching when working with "stacks" of nested monads.

Each monad transformer, such as `FutureT`, `OptionT` or `EitherT`, provides the code needed to merge its
related monad with other monads. The transformer is a data structure that wraps a monad stack, equipping 
it with `map` and `flatMap` methods that unpack and repack the whole stack.

The type signatures of monad transformers are written from the inside out, so an `EitherT[Option, String, A]`
is a wrapper for an `Option[Either[String, A]]`. It is often useful to use type aliases when writing 
transformer types for deeply nested monads.

With this look at monad transformers, we have now covered everything we need to know about monads and the
sequencing of computations using `flatMap`. In the next chapter we will switch tack and discuss two new type
classes, `Semigroupal` and `Applicative`, that support new kinds of operation such as `zipping` independent
values within a context.

## Semigroupal and Applicative

1. [Semigroupal](semigroupalAndApplicative/SemigroupalTypeClass.scala)

2. [Apply Syntax](semigroupalAndApplicative/ApplySyntax.scala)

3. [Semigroupal Applied to Different Types](semigroupalAndApplicative/SemigroupalAppliedToDifferentTypes.scala)

4. [Validated](semigroupalAndApplicative/ValidatedDataType.scala)

5. [Apply and Applicative](semigroupalAndApplicative/ApplyAndApplicative.scala)

### Summary

While monads and functors are the most widely used sequencing data types we've covered in this book, `semigroupal`
and `applicatives` are the most general. These type classes provide a generic mechanism to combine values and apply
functions within a context, from which we can fashion monads and a variety of other combinators.

`Semigroupal` and `Applicative` are most commonly used as a means of combining independent value such as the results
of validation rules. Cats provides the `Validated` type for this specific purpose, along with apply syntax as a
convenient way to express the combination of rules.

We have almost covered all of the functional programming concepts on our agenda for this book. The next chapter covers
`Traverse` and `Foldable`, two powerful type classes for converting between data types. After that we'll look at 
several case studies that bring together all of the concepts from Part I.

## Foldable and Traverse

1. [Foldable](foldableAndTraverse/FoldableTypeClass.scala)

2. [Traverse](foldableAndTraverse/TraverseTypeClass.scala)

### Summary

In this chapter we were introduced to `Foldable` and `Traverse`, two type classes for iterating over sequences.

`Foldable` abstracts the `foldLeft` and `foldRight` methods we know from collections in the standard library. It adds
stack-safe implementations of these methods to a handful of extra data types, and defines a host of situationally 
useful additions. That said, `Foldable` doesn't introduce much that we did not already know.

The real power comes from `Traverse`, which abstracts and generalises the `traverse` and sequence methods we know from
`Future`. Using these methods we can turn an `F[G[A]]` into a `G[F[A]]` for any `F` with an instance of `Traverse` and 
any `G` with an instance of `Applicative`. In terms of the reduction we get in lines of code, `Traverse` is one of the 
most powerful patterns in this book. We can reduce `folds` of many lines down to a single `foo.traverse`. 
