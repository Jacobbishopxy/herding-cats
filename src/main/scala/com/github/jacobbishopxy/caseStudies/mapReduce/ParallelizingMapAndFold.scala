package com.github.jacobbishopxy.caseStudies.mapReduce

/**
 * Created by Jacob on 2/17/2020
 *
 *
 */
object ParallelizingMapAndFold {

  /**
   * Recall the general signature for map is to apply a function `A => B` to a `F[A]`, returning a `F[B]`: `map`
   * transforms each individual element in a sequence independently. We can easily parallelize map because there
   * are no dependencies between the transformations applied to different elements (the type signature of the
   * function `A => B` shows us this, assuming we don't use side-effects not reflected in the types).
   *
   * What about fold? We can implement this step with an instance of `Foldable`. Not every functor also has an
   * instance of foldable but we can implement a map-reduce system on top of any data type that has both of these
   * type classes. Our reduction step becomes a `foldLeft` over the results of the distributed `map`.
   *
   * By distributing the reduce step we lose control over the order of traversal. Our overall reduction may not be
   * entirely left-to-right -- we may reduce left-to-right across several subsequences and then combine the results.
   * To ensure correctness we need a reduction operation that is associative:
   *
   * `reduce(a1, reduce(a2, a3)) == reduce(reduce(a1, a2), a3)`
   *
   * If we have associativity, we can arbitrarily distribute work between our nodes provided the subsequences at
   * every node stay in the same order as the initial dataset.
   *
   * Our fold operation requires us to seed the computation with an element of type `B`. Since fold may be split
   * into an arbitrary number of parallel steps, the seed should not affect the result of the computation. This
   * naturally requires the seed to be an `identity` element:
   *
   * `reduce(seed, a1) == reduce(a1, seed) == a1`
   *
   * In summary, our parallel fold will yield the correct results if:
   *
   * a. we require the reducer function to be associative;
   * b. we seed the computation with the identity of this function.
   *
   * What does this pattern sound like? That's right, we've come full circle back to `Monoid`. In this project we're
   * going to implement a very simple single-machine map-reduce. We'll start by implementing a method called `foldMap`
   * to model the data-flow we need.
   */

}
