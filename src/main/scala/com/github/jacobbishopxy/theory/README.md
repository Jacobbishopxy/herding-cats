# Theory

## Introduction

## Monoids and Semigroups

1. Definitions

2. Monoids in Cats

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

1. Definition

2. Functors in Cats

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
This allows us to parallelise or distribute transformations on large collections, a technique leverage 
heavily in "map-reduce" frameworks like Hadoop. We will investigate this approach in more detail in the
Map-reduce case study later in the book.

The Contravariant and Invariant type classes are less widely applicable but are still useful for building
data types that represent transformations. We will revisit them to discuss the `Semigroupal` type class later
in Chapter 6.

## Monads

1. Definition

2. Mondas in Cats

3. The Identity Monad

4. Either

5. Error Handling and MonadError

6. The Eval Monad

7. The Writer Monad

8. The Reader Monad

9. The State Monad

10. Defining Custom Monads

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

1. Composing Monads

2. Monad Transformers in Cats

3. Exercise: Monads: Transform and Roll Out

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

## Foldable and Traverse
