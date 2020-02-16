# Herding CATS

book source: [Scala with CATS](https://underscore.io/books/scala-with-cats/)

web source: [herding cats](http://eed3si9n.com/herding-cats/)

cats exercises: [Scala Cats exercises](https://www.scala-exercises.org/cats)

my study note: [CatsStudyNote](docs/CatsStudyNote.md)

## Content

### theory

[README](src/main/scala/com/github/jacobbishopxy/theory/README.md)

### herding cats

[README](src/main/scala/com/github/jacobbishopxy/herdingCats/README.md)

### case studies

[README](src/main/scala/com/github/jacobbishopxy/caseStudies/README.md)

## Note

### Cats Typeclass cheat sheet

[cheat sheet](docs/typeclasses.pdf)

1. Defining Signatures

|Typeclass|Signature|
| --- | --- |
|Functor|`F[A] => (A => B) => F[B]`|
|Contravariant|`F[A] => (B => A) => F[B]`|
|Apply|`F[A] => F[A => B] => F[B]`|
|FlatMap|`F[A] => (A => F[B]) => F[B]`|
|CoFlatMap|`F[A] => (F[A] => B) => F[B]`|
|Traverse|`F[A] => (A => G[B]) => G[F[B]]`|
|Foldable|`F[A] => (B, (B, A) => B) => B`|
|SemigroupK|`F[A] => F[A] => F[A]`|
|Cartesian|`F[A] => F[B] => F[(A, B)]`|

2. Derived Functions

|Typeclass|Function|Signature|
| --- | --- | --- |
|Functor|`map` <br/> `fproduct` <br/> `as` <br/> `tupleLeft` <br/> `tupleRight` <br/> `void`|`F[A] => (A => B) => F[B]` <br/> `F[A] => (A => B) => F[(A, B)]` <br/> `F[A] => B => F[B]` <br/> `F[A] => B => F[(B, A)]` <br/> `F[A] => B => F[(A, B)]` <br/> `F[A] => F[Unit]`|
|Contravariant|`contramap`|`F[A] => (B => A) => F[B]`|
|Apply|`ap` <br/> `map2`|`F[A] => F[A => B] => F[B]` <br/> `F[A] => (F[B] => ((A, B) => C)) => F[C]`|
|Applicative|`ap` <br/> `unlessA` <br/> `whenA` <br/> `replicateA`|`F[A] => F[A => B] => F[B]` <br/> `F[A] => Boolean => F[Unit]` <br/> `F[A] => Boolean => F[Unit]` <br/> `F[A] => Int => F[List[A]]`|
|FlatMap|`flatMap` <br/> `followedBy` <br/> `forEffect` <br/> `mproduct` <br/> `flatten`|`F[A] => (A => F[B]) => F[B]` <br/> `F[A] => F[B] => F[B]` <br/> `F[A] => F[B] => F[A]` <br/> `F[A] => (A => F[B]) => F[(A, B)]` <br/> `F[F[A]] => F[A]`|
|CoFlatMap|`coflatMap` <br/> `coflatten`|`F[A] => (F[A] => B) => F[B]` <br/> `F[A] => F[A[A]]`|
|Traverse|`traverse` <br/> `mapWithIndex` <br/> `zipWithIndex` <br/> `sequence`|`F[A] => (A => G[B]) => G[F[B]]` <br/> `F[A] => ((A, Int) => B) => F[B]` <br/> `F[A] => F[(A, Int)]` <br/> `F[G[A]] => G[F[A]]`|
|Foldable|`foldLeft` <br/> `foldRight` <br/> `foldMap` <br/> `combineAll` <br/> `find` <br/> `exists` <br/> `forall` <br/> `toList` <br/> `isEmpty` <br/> `nonEmpty` <br/> `size`|`F[A] => (B => ((B, A) => B)) => B` <br/> `F[A] => (Eval[B] => ((A, Eval[B]) => Eval[B])) => Eval[B]` <br/> `F[A] => (A => B) => B` <br/> `F[A] => A` <br/> `F[A] => (A => Boolean) => Option[A]` <br/> `F[A] => (A => Boolean) => Boolean` <br/> `F[A] => (A => Boolean) => Boolean` <br/> `F[A] => List[A]` <br/> `F[A] => Boolean` <br/> `F[A] => Boolean` <br/> `F[A] => Int`|
|SemigroupK|`combine`|`F[A] => F[A] => F[A]`|
|Cartesian|`product`|`F[A] => F[B] => F[(A, B)]`|

### Others

Monad type class hierarchy:

```

Cartesian            Functor
(product)             (map)
    |___________________|
              |
            Apply
            (ap)
     _________|_________
    |                   |
Applicative          FlatMap
  (pure)            (flatMap)
    |___________________|
              |
            Monad
```

Cats type class view:

thanks to tpolecat's [project](https://github.com/tpolecat/cats-infographic)

![Cats type class](docs/cats-type-class.svg)
