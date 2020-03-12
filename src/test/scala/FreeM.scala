

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

  /**
   * Real world applications often time combine different algebras.
   *
   * Let's see a trivial example of unrelated ADT's getting composed as a `EitherK` that can form a more complex
   * program.
   */

  import cats.data.EitherK
  import cats.free.Free
  import cats.{Id, InjectK, ~>}
  import scala.collection.mutable.ListBuffer

  // Handles user interaction
  sealed trait Interact[A]
  case class Ask(prompt: String) extends Interact[String]
  case class Tell(msg: String) extends Interact[Unit]

  // Represents persistence operations
  sealed trait DataOp[A]
  case class AddCat(a: String) extends DataOp[Unit]
  case class GetAllCats() extends DataOp[List[String]]

  // Once the ADTs are defined we can formally state that a `Free` program is the EitherK of its Algebras.
  type CatsApp[A] = EitherK[DataOp, Interact, A]

  // In order to take advantage of monadic composition we use smart constructors to lift our Algebra to
  // the `Free` context.
  class Interacts[F[_]](implicit I: InjectK[Interact, F]) {
    def tell(msg: String): Free[F, Unit] = Free.inject[Interact, F](Tell(msg))

    def ask(prompt: String): Free[F, String] = Free.inject[Interact, F](Ask(prompt))
  }

  object Interacts {
    implicit def interacts[F[_]](implicit I: InjectK[Interact, F]): Interacts[F] =
      new Interacts[F]
  }

  class DataSource[F[_]](implicit I: InjectK[DataOp, F]) {
    def addCat(a: String): Free[F, Unit] = Free.inject[DataOp, F](AddCat(a))

    def getAllCats: Free[F, List[String]] = Free.inject[DataOp, F](GetAllCats())
  }

  object DataSource {
    implicit def dataSource[F[_]](implicit I: InjectK[DataOp, F]): DataSource[F] =
      new DataSource[F]
  }

  // ADTs are now easily composed and trivially intertwined inside monadic contexts.
  def program(implicit I: Interacts[CatsApp], D: DataSource[CatsApp]): Free[CatsApp, Unit] = {
    import I._, D._

    for {
      cat <- ask("What's the kitty's name?")
      _ <- addCat(cat)
      cats <- getAllCats
      _ <- tell(cats.toString)
    } yield ()
  }

  // Finally we write one interpreter per ADT and combine them with a `FunctionK` to `EitherK` so they can
  // be compiled and applied to our `Free` program.
  object ConsoleCatsInterpreter extends (Interact ~> Id) {
    override def apply[A](fa: Interact[A]): Id[A] = fa match {
      case Ask(prompt) =>
        println(prompt)
        scala.io.StdIn.readLine
      case Tell(msg) =>
        println(msg)
    }
  }

  object InMemoryDatasourceInterpreter extends (DataOp ~> Id) {
    private[this] val memDataSet = new ListBuffer[String]

    override def apply[A](fa: DataOp[A]): Id[A] = fa match {
      case AddCat(a) =>
        memDataSet.append(a)
        ()
      case GetAllCats() =>
        memDataSet.toList
    }
  }

  val interpreter: CatsApp ~> Id = InMemoryDatasourceInterpreter or ConsoleCatsInterpreter

  // Now if we run our program and type in "snuggles" when prompted, we see something like this:

  import DataSource._
  import Interacts._

  val evaled: Unit = program.foldMap(interpreter)

}


/**
 * Understanding free monads
 */

object ABusinessCase {

  type Symbol = String
  type Response = String

  sealed trait Orders[A]
  case class Buy(stock: Symbol, amount: Int) extends Orders[Response]
  case class Sell(stock: Symbol, amount: Int) extends Orders[Response]

  import cats.free.Free

  type OrdersF[A] = Free[Orders, A]

  def buy(stock: Symbol, amount: Int): OrdersF[Response] =
    Free.liftF[Orders, Response](Buy(stock, amount))

  def sell(stock: Symbol, amount: Int): OrdersF[Response] =
    Free.liftF[Orders, Response](Sell(stock, amount))

  val smartTrade: Free[Orders, Response] = for {
    _ <- buy("APPL", 50)
    _ <- buy("MSFT", 10)
    rsp <- sell("GOOG", 200)
  } yield rsp

  /**
   * This code still does nothing else than defining the steps, we have now way to obtain a result from it.
   * We need a way to execute, or interpret our language.
   */

}

object OurFirstInterpreter extends App {

  import ABusinessCase._

  /**
   * An interpreter is something that will read our program and do something with it. Technically an interpreter
   * is a `natural transformation`. The key thing to know is that an interpreter requires a monad as the end
   * part of the transformation. This means you can use an interpreter to obtain `Option`, `Xor`, or some other
   * monad, but not to obtain anything that is not a monad.
   */

  import cats.{Id, ~>}

  def orderPrinter: Orders ~> Id =
    new (Orders ~> Id) {
      override def apply[A](fa: Orders[A]): Id[A] = fa match {
        case Buy(stock, amount) =>
          println(s"Buying $amount of $stock")
          "ok"
        case Sell(stock, amount) =>
          println(s"Selling $amount of $stock")
          "ok"
      }
    }


  /**
   * This squiggly sign `~>` is the syntax sugar for `natural transformation`. Note that in the interpreter we do
   * a pattern match over each member of our language. As `Buy` is of type `Order[Response]` (equivalent to
   * `Order[String]` in this scenario), the method signature forces us to return a result of `Id[String]`.
   * The same for `Sell`.
   *
   * We are also executing some `println` statements before returning the result. The only restriction given by
   * the signature is the return type, we can have side effects in our code (as we do in this case). Obviously
   * this is not advisable, but it can be useful when we create interpreters for testing purposes.
   *
   * We have our interpreter, which means that we have all the pieces we need to execute the program. We can do
   * this via the `foldMap` operation:
   */

  smartTrade.foldMap(orderPrinter)
}

object EitherInterpreters extends App {

  import ABusinessCase._

  /**
   * `Id` is not so useful, and we want to avoid side effects in our code. If we aim to do something akin to railway
   * oriented programming we may want to use `Either` instead.
   *
   * But this reveals a slight issue: the natural transformation expects a monad with shape `G[_]`, and `Either` is
   * `Either[+A, +B]`. There is a mismatch in the number of holes. Thankfully we can fix that with a small trick,
   * by fixing the type of the left side of `Either`, like:
   * `type ErrorOr[A] = Either[String, A]`
   *
   * This creates a new monadic type with a single type parameter, which fits the requirements of natural transformation.
   * You may want to use an ADT instead of `String` on the left side, to make it more flexible. In any case, we can
   * now construct a new interpreter:
   */

  type ErrorOr[A] = Either[String, A]

  import cats.~>
  import cats.syntax.either._
  import cats.implicits._

  def eitherInterpreter: Orders ~> ErrorOr =
    new (Orders ~> ErrorOr) {
      override def apply[A](fa: Orders[A]): ErrorOr[A] = fa match {
        case Buy(stock, amount) =>
          s"$stock - $amount".asRight
        case Sell(stock, amount) =>
          "Why are you selling that?".asLeft

      }
    }

  smartTrade.foldMap(eitherInterpreter)
}

































