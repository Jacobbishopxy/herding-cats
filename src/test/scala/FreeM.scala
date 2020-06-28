

/**
 * Created by Jacob Xie on 2/24/2020
 *
 *
 * There are five basic steps to "freeing" the ADT:
 *
 * 1. Create a type based on `Free[_]` and `KVStoreA[_]`.
 * 2. Create smart constructors for `KVStore[_]` using `liftF`.
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
   *
   * 定义KVStoreA的Free类型投影
   */

  import cats.free.Free

  type KVStore[A] = Free[KVStoreA, A]


  /**
   * 2. Create smart constructors using `liftF`
   *
   * These methods will make working with our DSL a lot nicer, and will lift `KVStoreA[_]` values into our `KVStore`
   * monad (not the missing "A" in the second type).
   *
   * 通过liftF将KVStoreA的ADT提升到其Free类型的投影，liftF的目的是：
   * 1. 将计算升级为纯函数，使用不可变的值；
   * 2. 将程序的创建和执行分离开；
   * 3. 能够支持许多不同的执行方法。
   */

  // Put returns nothing (i.e. Unit).
  def put[T](key: String, value: T): KVStore[Unit] =
    Free.liftF[KVStoreA, Unit](Put[T](key, value))

  // Get returns a T value.
  def get[T](key: String): KVStore[Option[T]] =
    Free.liftF[KVStoreA, Option[T]](Get[T](key))

  // Delete returns nothing (i.e. Unit).
  def delete(key: String): KVStore[Unit] =
    Free.liftF(Delete(key))

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
   *
   * 关注分离：用DSL来描述业务运算
   * program看上去貌似一个monadic flow，但是事实上这里只是构建了一个递归运算的结构（分离了关注），也就是说Free只是被用于这些
   * 嵌入式DSL来构建执行流程的，这个流程并不能被直接执行。如果我们试图允许program，实际上只会得到一个Free[_]结构。
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
   * In our case, we will use a simple mutable map to represent our key value store.
   *
   * 因此接下来我们需要一个能够执行这个流程的“编译器”，也就是实现左伴随F[_] -> G[_]过程，得到真正的Monad。Cats为此提供了一个
   * FunctionK[F, G]函数（syntax语法为~>）来封装这个过程。
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

  // In order to take advantage of monadic composition we use smart constructors to lift our Algebra to the `Free` context.
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

object UsingFreeT extends App {

  /**
   * Often times we want to interleave the syntax tree when building a Free monad with some other effect not declared
   * as part of the ADT. FreeT solves this problem by allowing us to mix building steps of the AST with calling action
   * in other base monad.
   *
   * In the following example a basic console application is shown. When the user inputs some text we use a separate
   * `State` monad to track what the user typed.
   *
   * As we can observe in this case `FreeT` offers us the alternative to delegate denotations to `State` monad with
   * stronger equational guarantees than if we were emulating the `State` ops in our own ADT.
   */

  import cats.free._
  import cats._
  import cats.data._

  // A base ADT for the user interaction without state semantics
  sealed abstract class Teletype[A] extends Product with Serializable
  final case class WriteLine(line: String) extends Teletype[Unit]
  final case class ReadLine(prompt: String) extends Teletype[String]

  type TeletypeT[M[_], A] = FreeT[Teletype, M, A]
  type Log = List[String]
  type TeletypeState[A] = State[List[String], A]

  // Teletype smart constructors
  object TeletypeOps {
    def writeLine(line: String): TeletypeT[TeletypeState, Unit] =
      FreeT.liftF[Teletype, TeletypeState, Unit](WriteLine(line))

    def readLine(prompt: String): TeletypeT[TeletypeState, String] =
      FreeT.liftF[Teletype, TeletypeState, String](ReadLine(prompt))

    def logs(s: String): TeletypeT[TeletypeState, Unit] =
      FreeT.liftT[Teletype, TeletypeState, Unit](State.modify(s :: _))
  }

  def program: TeletypeT[TeletypeState, Unit] =
    for {
      userSaid <- TeletypeOps.readLine("what's up?!")
      _ <- TeletypeOps.logs(s"user said:  $userSaid")
      _ <- TeletypeOps.writeLine("thanks, see you soon!")
    } yield ()

  def interpreter: Teletype ~> TeletypeState = new (Teletype ~> TeletypeState) {
    override def apply[A](fa: Teletype[A]): TeletypeState[A] =
      fa match {
        case ReadLine(prompt) =>
          println(prompt)
          val userInput = "hanging in here"
          StateT.pure[Eval, List[String], A](userInput)
        case WriteLine(line) =>
          StateT.pure[Eval, List[String], A](println(line))
      }
  }

  val state = program.foldMap(interpreter)

  val initialState = Nil

  val (stored, _) = state.run(initialState).value

}

object UsingFreeT2 extends App {

  import cats.free._
  import cats._
  import cats.data._
  import cats.implicits._
  import scala.util.Try

  sealed trait Ctx[A]

  case class Action(value: Int) extends Ctx[Int]

  def op1: FreeT[Ctx, Option, Int] =
    FreeT.liftF[Ctx, Option, Int](Action(7))

  def op2: FreeT[Ctx, Option, Int] =
    FreeT.liftT[Ctx, Option, Int](Some(4))

  def op3: FreeT[Ctx, Option, Int] =
    FreeT.pure[Ctx, Option, Int](1)

  def opComplete: FreeT[Ctx, Option, Int] =
    for {
      a <- op1
      b <- op2
      c <- op3
    } yield a + b + c


  type OptTry[A] = OptionT[Try, A]

  def tryInterpreter: Ctx ~> OptTry = new (Ctx ~> OptTry) {
    override def apply[A](fa: Ctx[A]): OptTry[A] =
      fa match {
        case Action(value) => OptionT.liftF(Try(value))
      }
  }

  def optTryLift: Option ~> OptTry = new (Option ~> OptTry) {
    override def apply[A](fa: Option[A]): OptTry[A] =
      fa match {
        case Some(value) => OptionT(Try(Option(value)))
        case None => OptionT.none
      }
  }

  val hoisted = opComplete.hoist(optTryLift)

  val evaluated = hoisted.foldMap(tryInterpreter)

  val result = evaluated.value

}

