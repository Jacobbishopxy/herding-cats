

/**
 * Created by Jacob Xie on 2/24/2020
 *
 *
 * There are five basic steps to "freeing" the ADT:
 *
 * 1. Create a type based on `Free[_]` and `KVStoreA[_]`.
 * 2. Create smart constructors for `KVStore[_]` using `liftE`.
 * 3. Build a program out of key-value DSL operations.
 * 4. Build a compiler for programs of DSL operations.
 * 5. Execute our compiled program.
 */
object UsingFreeMonads extends App {

  /**
   * 0. Create an ADT representing your grammar
   */

  sealed trait KVStoreA[A]

  case class Put[T](key: String, value: T) extends KVStoreA[Unit]
  case class Get[T](key: String) extends KVStoreA[Option[T]]
  case class Delete(key: String) extends KVStoreA[Unit]


  /**
   * 1. Create a `Free` type based on your ADT
   */

  import cats.free.Free

  type KVStore[A] = Free[KVStoreA, A]


  /**
   * 2. Create smart constructors using `liftE`
   *
   * These methods will make working with our DSL a lot nicer, and will lift `KVStoreA[_]` values into our `KVStore`
   * monad (not the missing "A" in the second type).
   */

  import cats.free.Free.liftF

  // Put returns nothing (i.e. Unit).
  def put[T](key: String, value: T): KVStore[Unit] =
    liftF[KVStoreA, Unit](Put[T](key, value))


  // Get returns a T value.
  def get[T](key: String): KVStore[Option[T]] =
    liftF[KVStoreA, Option[T]](Get[T](key))

  // Delete returns nothing (i.e. Unit).
  def delete(key: String): KVStore[Unit] =
    liftF(Delete(key))

  // Update composes get and set, and returns nothing.
  def update[T](key: String, f: T => T): KVStore[Unit] =
    for {
      vMaybe <- get[T](key)
      _ <- vMaybe.map(v => put[T](key, f(v))).getOrElse(Free.pure(()))
    } yield ()


  /**
   * 3. Build a program
   *
   * Now that we can construct `KVStore[_]` values we can use our DSL to write "programs" using a for-comprehension.
   * It looks like a monadic flow. However, it just builds a recursive data structure representing the sequence of
   * operations.
   */

  def program: KVStore[Option[Int]] =
    for {
      _ <- put("wild-cats", 2)
      _ <- update[Int]("wild-cats", _ + 12)
      _ <- put("tame-cats", 5)
      n <- get[Int]("wild-cats")
      _ <- delete("tame-cats")
    } yield n


  /**
   * 4. Write a compiler for your program
   *
   * As you may have understood now, `Free[_]` is used to create an embedded DSL. By itself, this DSL only represents
   * a sequence of operations (defined by a recursive data structure); it doesn't produce anything.
   *
   * `Free[_]` is a programming language inside your programming language!
   *
   * So, like any other programing language, we need to compile our abstract language into an effective language and
   * then run it.
   *
   * To do this, we will use a natural transformation between type containers. Natural transformations go between
   * types like `F[_]` and `G[_]` (this particular transformation would be written as `FunctionK[F, G]` or as done
   * here using the symbolic alternative as `F ~> G`).
   *
   * In our case, we will use a simple mutable map to represent our key value store:
   */

  import cats.arrow.FunctionK
  import cats.{Id, ~>}
  import scala.collection.mutable

  // the program will crash if a key is not found, or if a type is incorrect specified
  def impureCompiler: KVStoreA ~> Id =
    new (KVStoreA ~> Id) {

      // a very simple (and imprecise) key-value store
      val kvs = mutable.Map.empty[String, Any]

      def apply[A](fa: KVStoreA[A]): Id[A] =
        fa match {
          case Put(key, value) =>
            println(s"put($key, $value)")
            kvs(key) = value
            ()
          case Get(key) =>
            println(s"get($key)")
            kvs.get(key).map(_.asInstanceOf[A])
          case Delete(key) =>
            println(s"delete($key)")
            kvs.remove(key)
            ()
        }

    }

  /**
   * Please note this `impureCompiler` is impure -- it mutates kvs and also produces logging output using `println`.
   * The whole purpose of functional programming isn't to prevent side-effects, it is just to push side-effects to
   * the boundaries of you system in a well-known and controlled way.
   *
   * `Id[_]` represents the simplest type container: the type itself. Thus, `Id[Int]` is just `Int`. This means that
   * our program will execute immediately, and block until the final value can be returned.
   *
   * However, we could easily use other type containers for different behavior, such as:
   *
   * - `Future[_]` for asynchronous computation
   * - `List[_]` for gathering multiple results
   * - `Option[_]` to support optional results
   * - `Either[E, *]` to support failure
   * - a pseudo-random monad to support non-determinism
   * - and so on...
   */

  /**
   * 5. Run your program
   *
   * The final step is naturally running your program after compiling it.
   *
   * `Free[_]` is just a recursive structure that can be seen as sequence of operations producing other operations.
   * In this way it is similar to `List[_]`. We often use folds (e.g. `foldRight`) to obtain a single value from
   * a list; this recurse over the structure, combining its contents.
   *
   * The idea behind running a `Free[_]` is exactly the same. We fold the recursive structure by:
   *
   * - consuming each operation.
   * - compiling the operation into our effective language using `impureCompiler` (applying its effects if any).
   * - computing next operation.
   * - continue recursively until reaching a `Pure` state, and returning it.
   *
   * This operation is called `Free.foldMap`:
   * `final def foldMap[M[_]](f: FunctionK[S, M])(M: Monad[M]): M[A] = ...`
   *
   * `M` must be a `Monad` to be flattenable (the famous monoid aspect under `Monad`). As `Id` is a `Monad`, we can
   * use `foldMap`.
   *
   * To run your `Free` with previous `impureCompiler`:
   */

//  val result: Option[Int] = program.foldMap(impureCompiler)

  /**
   * An important aspect of `foldMpa` is its stack-safety. It evaluates each step of computation on the stack then
   * unstack and restart. This process is known as trampolining.
   *
   * As long as your natural transformation is stack-safe, `foldMap` will never overflow your stack. Trampolining
   * is heap-intensive but stack-safety provides the reliability required to use `Free[_]` for data-intensive tasks,
   * as well as infinite processes such as streams.
   */

  /**
   * 6. Use a pure compiler (optional)
   *
   * The previous examples used an effective natural transformation. This works, but you might prefer folding you
   * `Free` in a "pure" way. The State data structure can be used to keep track of the program state in an immutable
   * map, avoiding mutation altogether.
   */

  import cats.data.State

  type KVStoreState[A] = State[Map[String, Any], A]

  val pureCompiler: KVStoreA ~> KVStoreState = new (KVStoreA ~> KVStoreState) {
    override def apply[A](fa: KVStoreA[A]): KVStoreState[A] =
      fa match {
        case Put(key, value) => State.modify(_.updated(key, value))
        case Get(key) => State.inspect(_.get(key).map(_.asInstanceOf[A]))
        case Delete(key) => State.modify(_ - key)
      }
  }

  val resultP: (Map[String, Any], Option[Int]) = program.foldMap(pureCompiler).run(Map.empty).value

}


object ComposingFreeMonadsADTs extends App {

}

