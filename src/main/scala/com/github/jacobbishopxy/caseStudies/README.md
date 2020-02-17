# Case Studies


## 1. Testing Asynchronous Code

1. Abstracting Over Type Constructors

2. Abstracting Over Monads

### Summary

This case study provides an example of how Cats can help us abstract over different computational scenarios. We used
the `Applicative` type class to abstract over asynchronous and synchronous code. Leaning on a functional abstraction
allows us to specify the sequence of computations we want to perform without worrying about the details of the
implementation.

Type classes like `Functor`, `Applicative`, `Monad`, and `Traverse` provide abstract implementations of patterns 
such as mapping, zipping, sequencing, and iteration. The mathematical laws on those types ensure that they work 
together with a consistent set of semantics.

## 2. Map-Reduce

1. Parallelizing map and fold

2. Implementing foldMap

3. Parallelising foldMap

    - Futures, Thread Pools, and ExecutionContexts
    
    - Dividing Work
    
    - Implementing parallelFoldMap
    
    - parallelFoldMap with more Cats

## 3. Data Validation

## 4. CRDTs

