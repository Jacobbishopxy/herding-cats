package com.github.jacobbishopxy.theory.monadTransformers

/**
 * Created by jacob on 2/5/2020
 *
 * Each monad transformer is a data type, defined in `cats.data`, that allows us to wrap stacks of
 * monads to produce new monads. We use the monads we've built via the Monad type class. The main
 * concepts we have to cover to understand monad transformers are:
 *
 * a. the available transformer classes;
 * b. how to build stacks of monads using transformers;
 * c. how to construct instances of a monad stack;
 * d. how to pull apart a stack to access the wrapped monads.
 */
object MonadTransformersInCats {

  /**
   * 1. The Monad Transformer Classes
   *
   * By convention, in Cats a monad Foo will have a transformer class called FooT. In fact, many
   * monads in Cats are defined by combining a monad transformer with the `Id` monad. Concretely,
   * some of the available instances are
   *
   * - `cats.data.OptionT` for `Option`;
   * - `cats.data.EitherT` for `Either`;
   * - `cats.data.ReaderT` for `Reader`;
   * - `cats.data.WriterT` for `Writer`;
   * - `cats.data.StateT` for `State`;
   * - `cats.data.IdT` for the `Id` monad.
   */

  /**
   * 2. Building Monad Stacks
   *
   * All of these monad transformers follow the same convention. The transformer itself represents
   * the inner monad in a stack, while the first type parameter specifies the outer monad. The
   * remaining type parameters are the types we've used to form the corresponding monads.
   *
   * For example, our `ListOption` type above is an alias for `OptionT[List, A]` but the result is
   * effectively a `List[Option[A]]`. In other words, we build monad stacks from the inside out:
   * `type ListOption[A] = OptionT[List, A]`
   *
   * Many monads and all transformers have at least two type parameters, so we often have to define
   * type aliases for intermediate stages.
   *
   * For example, suppose we want to wrap `Either` around `Option`. `Option` is the innermost type
   * so we want to use the `OptionT` monad transformer. We need to use `Either` as the first type
   * parameter. However, `Either` itself has two type parameters and monads only have one. We need
   * a type alias to convert the type constructor to the correct shape:
   */

  import cats.data.OptionT

  type ErrorOr[A] = Either[String, A]

  type ErrorOrOption[A] = OptionT[ErrorOr, A]

  /**
   * `ErrorOrOption` is a monad, just like `ListOption`. We can use `pure`, `map`, and `flatMap` as
   * usual to create and transform instances:
   */

  import cats.instances.either._ // for Monad
  import cats.syntax.applicative._

  val a = 10.pure[ErrorOrOption]
  // a: ErrorOrOption[Int] = OptionT(Right(Some(10)))
  val b = 32.pure[ErrorOrOption]
  // b: ErrorOrOption[Int] = OptionT(Right(Some(32)))
  val c = a.flatMap(x => b.map(y => x + y))
  // c: cats.data.OptionT[ErrorOr,Int] = OptionT(Right(Some(42)))

  /**
   * Things become even more confusing when we want to stack three or more monads.
   *
   * For example, let's create a `Future` of an `Either` of `Option`. Once again we build this from
   * the inside out with an `OptionT` of an `EitherT` of `Future`. However, we can't define this in
   * one line because `EitherT` has three type parameters:
   * `
   * case class EitherT[F[_], E, A](stack: F[Either[E, A]]) {
   * // etc...
   * }
   * `
   *
   * The three type parameters are as follows:
   * a. F[_] is the outer monad in the stack (Either is the inner);
   * b. E is the error type for the Either;
   * c. A is the result type for the Either.
   *
   * This time we create an alias for `EitherT` that fixes `Future` and `Error` and allows A to vary:
   */

  import scala.concurrent.Future
  import cats.data.{EitherT, OptionT}

  type FutureEither[A] = EitherT[Future, String, A]
  type FutureEitherOption[A] = OptionT[FutureEither, A]

  // Our mammoth stack now composes three monads and our `map` and `flatMap` methods cut through three
  // layers of abstraction:

  import cats.instances.future._
  import scala.concurrent.Await
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._

  val futureEitherOr: FutureEitherOption[Int] =
    for {
      a <- 10.pure[FutureEitherOption]
      b <- 32.pure[FutureEitherOption]
    } yield a + b

  /**
   * Kind Projector
   *
   * If you frequently find your self defining multiple type aliases when building monad stacks, you may
   * want to try the Kind Projector compiler plugin. Kind Projector enhances Scala's type syntax to make it
   * easier to define partially applied type constructors. For example:
   */

  import cats.instances.option._

  123.pure[EitherT[Option, String, *]]
  // cats.data.EitherT[Option,String,Int] = EitherT(Some(Right(123)))

  /**
   * 3. Constructing and Unpacking Instances
   *
   * As we saw above, we can create transformed monad stacks using the relevant monad transformer's
   * `apply` method or the usual `pure` syntax:
   */

  // Create using apply:
  val errorStack1 = OptionT[ErrorOr, Int](Right(Some(10)))
  // errorStack1: cats.data.OptionT[ErrorOr,Int] = OptionT(Right(Some(10)))

  // Create using pure:
  val errorStack2 = 32.pure[ErrorOrOption]
  // errorStack2: ErrorOrOption[Int] = OptionT(Right(Some(32)))

  /**
   * Once we've finished with a monad transformer stack, we can unpack it using its `value` method. This
   * returns the untransformed stack. We can then manipulate the individual monads in the usual way:
   */

  // Extracting the untransformed monad stack:
  val e1 = errorStack1.value
  // e1: ErrorOr[Option[Int]] = Right(Some(10))

  // Mapping over the Either in the stack:
  val e2 = errorStack2.value.map(_.getOrElse(-1))
  // e2: scala.util.Either[String,Int] = Right(32)

  /**
   * Each call to value unpacks a single monad transformer. We may need more than one call to completely
   * unpack a large stack. For example, to `Await` the `FutureEitherOption` stack above, we need to cal
   * value twice:
   */
  val intermediate = futureEitherOr.value
  // intermediate: FutureEither[Option[Int]] = EitherT(Future(Success(Right(Some(42)))))
  val stack = intermediate.value
  // stack: scala.concurrent.Future[Either[String,Option[Int]]] = Future(Success(Right(Some(42))))

  Await.result(stack, 1.second)
  // Either[String, Option[Int]] = Right(Some(42))

  /**
   * 4. Defaults Instances
   *
   * Many monads in Cats are defined using the corresponding transformer and the `Id` monad. This is
   * reassuring as it confirms that the APIs for monads and transformers are identical. `Reader`, `Writer`,
   * and `State` are all defined in this way:
   * `
   * type Reader[E, A] = ReaderT[Id, E, A] // = Kleisli[Id, E, A]
   * type Writer[W, A] = WriterT[Id, W, A]
   * type State[S, A] = StateT[Id, S, A]
   * `
   *
   * In other cases monad transformers are defined separately to their corresponding monads. In these cases,
   * the methods of the transformer tend to mirror the methods on the monad. For example, `OptionT` defines
   * `getOrElse`, and `EitherT` defines `fold`, `bimap`, `swap`, and other useful methods.
   */
}

object MonadTransformersUsagePatterns {

  /**
   * 5. Usage Patterns
   *
   * Widespread use of monad transformers is sometimes difficult because they fuse monads together in
   * predefined ways. Without careful thought, we can end up having to unpack and repack monads in different
   * configurations to operate on them in different contexts.
   *
   * We can cope with this in multiple ways. One approach involves creating a single "super stack" and
   * sticking to it throughout our code base. This works if the code simple and largely uniform in nature.
   * For example, in a web application, we could decide that all request handlers are asynchronous and all
   * can fail with the same set of HTTP error codes. We could design a custom ADT representing the errors
   * and use a fusion `Future` and `Either` everywhere in our code:
   */

  import cats.data.EitherT
  import scala.concurrent.Future

  sealed abstract class HttpError
  final case class NotFound(item: String) extends HttpError
  final case class BadRequest(msg: String) extends HttpError
  // etc...

  type FutureEither[A] = EitherT[Future, HttpError, A]

  /**
   * The "super stack" approach starts to fail in larger, more heterogeneous code bases where different stacks
   * make sense in different contexts. Another design pattern that makes more sense in these contexts uses
   * monad transformers as local "glue code". We expose untransformed stacks at module boundaries, transform
   * them to operate on them locally, and un-transform them before passing them on. This allows each module of
   * code to make its own decisions about which transformers to use:
   */

  import cats.data.Writer
  import cats.instances.list._


  type Logged[A] = Writer[List[String], A]

  // Methods generally return untransformed stacks:
  def parseNumber(str: String): Logged[Option[Int]] =
    util.Try(str.toInt).toOption match {
      case Some(num) => Writer(List(s"Read $str"), Some(num))
      case None => Writer(List(s"Failed on $str"), None)
    }

  // Consumers use monad transformers locally to simplify composition:
  def addAll(a: String, b: String, c: String): Logged[Option[Int]] = {
    import cats.data.OptionT

    val result: OptionT[Logged, Int] = for {
      a <- OptionT(parseNumber(a))
      b <- OptionT(parseNumber(b))
      c <- OptionT(parseNumber(c))
    } yield a + b + c

    result.value
  }

  // This approach doesn't force OptionT on other user's code:
  val result1 = addAll("1", "2", "3")
  // result1: Logged[Option[Int]] = WriterT((List(Read 1, Read 2, Read 3), Some(6)))

  val result2 = addAll("1", "a", "3")
  // result2: Logged[Option[Int]] = WriterT((List(Read 1, Failed on a), None))

  /**
   * Unfortunately, there aren't one-size-fits-all approaches to working with monad transformers. The best
   * approach for you may depend on a lot of factors: the size and experience of your team, the complexity of
   * your code base, and so on. You may need to experiment and gather feedback from colleagues to determine
   * whether monad transformers are a good fit.
   */
}
