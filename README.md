# Herding CATS

book source: [Scala with CATS](https://underscore.io/books/scala-with-cats/)

web source: [herding cats](http://eed3si9n.com/herding-cats/)


## Content

### theory

[README](https://github.com/Jacobbishopxy/herding-cats/tree/master/src/main/scala/com/github/jacobbishopxy/theory)

### herding cats

[README](https://github.com/Jacobbishopxy/herding-cats/tree/master/src/main/scala/com/github/jacobbishopxy/herdingCats)

## Note

- pure:

    `A => F[A]`

- map:

    `(F[A], A => B) => F[B]`

- flatMap:

    `(F[A], A => F[B]) => F[B]`

- product:

    `(F[A], F[B]) => F[(A, B)]`

- ap:

    `(F[A], F[A => B]) => F[B]`
    

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
