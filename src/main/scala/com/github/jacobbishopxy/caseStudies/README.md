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

### Summary

In this case study we implemented a system that imitates map-reduce as performed on a cluster. Our algorithm followed
three steps:

    1. batch the data and send one batch to each "node";
    2. perform a local map-reduce on each batch;
    3. combine the results using monoid addition.

Our toy system emulates the batching behaviour of real-world map-reduce systems such as Hadoop. However, in reality
we are running all of our work on a single machine where communication between nodes is negligible. We don't actually
need to batch data to gain efficient parallel processing of a list. We can simply map using a Functor and reduce 
using a Monoid.

Regardless of the batching strategy, mapping and reducing with Monoids is a powerful and general framework that isn't
limited to simple tasks like addition and string concatenation. Most of the tasks data scientists perform in their
day-to-day analyses can be cast as monoids. There are monoids for all the following:

    - approximate sets such as the Bloom filter;
    - set cardinality estimators, such as HyperLogLog algorithm;
    - vectors and vector operations like stochastic gradient descent;
    - quantile estimators such as the t-digest.

## 3. Data Validation

## 4. CRDTs

