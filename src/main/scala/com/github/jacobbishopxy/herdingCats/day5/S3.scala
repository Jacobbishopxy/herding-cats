package com.github.jacobbishopxy.herdingCats.day5

/**
 * Created by jacob on 2/14/2020
 *
 * List datatype
 *
 * LYAHFGG:
 *
 * > On the other hand, a value like [3,8,9] contains several results, so we can view it as one value that is
 * actually many values at the same time. Using lists as applicative functors showcases this non-determinism nicely.
 */
object S3 {

  import cats._
  import cats.data._
  import cats.implicits._

  // Let’s look at using List as Applicatives again:
  (List(1, 2, 3), List(10, 100, 100)) mapN { _ * _ }
  // List(10, 100, 100, 20, 200, 200, 30, 300, 300)

  // let’s try feeding a non-deterministic value to a function:

  List(3, 4, 5) >>= { x => List(x, -x) }
  // List(3, -3, 4, -4, 5, -5)

  /**
   * So in this monadic view, a List context represents a mathematical value that could have multiple solutions.
   * Other than that manipulating Lists using for notation is just like plain Scala:
   */

  for {
    n <- List(1, 2)
    ch <- List('a', 'b')
  } yield (n, ch)
  // List((1,a), (1,b), (2,a), (2,b))

}
