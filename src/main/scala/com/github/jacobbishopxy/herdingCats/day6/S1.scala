package com.github.jacobbishopxy.herdingCats.day6

/**
 * Created by jacob on 2/15/2020
 *
 * Writer datatype
 *
 * Learn You a Haskell for Great Good says:
 *
 * > Whereas the Maybe monad is for values with an added context of failure, and the list monad is for
 * nondeterministic values, Writer monad is for values that have another value attached that acts as a sort of log value.
 */
object S1 {

  import cats._
  import cats.data._
  import cats.implicits._

  /**
   * LYAHFGG:
   *
   * > To attach a monoid to a value, we just need to put them together in a tuple. The Writer w a type is just a
   * new type wrapper for this.
   */

  // In Cats, the equivalent is called `Writer`:

  //  type Writer[L, V] = WriterT[Id, L, V]
  //  object Writer {
  //    def apply[L, V](l: L, v: V): WriterT[Id, L, V] = WriterT[Id, L, V]((l, v))
  //
  //    def value[L:Monoid, V](v: V): Writer[L, V] = WriterT.value(v)
  //
  //    def tell[L](l: L): Writer[L, Unit] = WriterT.tell(l)
  //  }

  // Writer[L, V] is a type alias for WriterT[Id, L, V]

  /**
   * WriterT
   * Here’s the simplified version of `WriterT`:
   */

  //    final case class WriterT[F[_], L, V](run: F[(L, V)]) {
  //      def tell(l: L)(implicit functorF: Functor[F], semigroupL: Semigroup[L]): WriterT[F, L, V] =
  //        mapWritten(_ |+| l)
  //
  //      def written(implicit functorF: Functor[F]): F[L] =
  //        functorF.map(run)(_._1)
  //
  //      def value(implicit functorF: Functor[F]): F[V] =
  //        functorF.map(run)(_._2)
  //
  //      def mapBoth[M, U](f: (L, V) => (M, U))(implicit functorF: Functor[F]): WriterT[F, M, U] =
  //        WriterT { functorF.map(run)(f.tupled) }
  //
  //      def mapWritten[M](f: L => M)(implicit functorF: Functor[F]): WriterT[F, M, V] =
  //        mapBoth((l, v) => (f(l), v))
  //    }

  // Here’s how we can create Writer values:

  val w: WriterT[Id, String, Int] = Writer("Smallish gang.", 3)
  val v: Writer[String, Int] = Writer.value[String, Int](3)
  //val l = Writer[String, Unit] = WriterT((Log something, ()))

  val wRun: (String, Int) = w.run // (Smallish gang., 3)

  /**
   * Using for syntax with Writer
   * LYAHFGG:
   *
   * > Now that we have a Monad instance, we’re free to use do notation for Writer values.
   */

  def logNumber(x: Int): Writer[List[String], Int] =
    Writer(List("Got number: " + x.show), 3)

  def multWithLog: WriterT[Id, List[String], Int] =
    for {
      a <- logNumber(3)
      b <- logNumber(5)
    } yield a * b

  multWithLog.run // (List(Got number: 3, Got number: 5),9)

  /**
   * Adding logging to program
   *
   * Here’s the gcd example:
   */

  def gcd(a: Int, b: Int): Writer[List[String], Int] =
    if (b == 0) for {
      _ <- Writer.tell(List("Finished with " + a.show))
    } yield a
    else Writer.tell(List(s"${a.show} mod ${b.show} = ${(a % b).show}")) >>= { _ =>
      gcd(b, a % b)
    }

  gcd(12, 16) // (List(12 mod 16 = 12, 16 mod 12 = 4, 12 mod 4 = 0, Finished with 4),4)

  /**
   * Inefficient List construction
   * LYAHFGG:
   *
   * > When using the Writer monad, you have to be careful which monoid to use, because using lists
   * can sometimes turn out to be very slow. That’s because lists use ++ for mappend and using ++ to add
   * something to the end of a list is slow if that list is really long.
   *
   * Here’s the Vector version of gcd:
   */

  def gcdV(a: Int, b: Int): Writer[Vector[String], Int] = {
    if (b == 0) for {
      _ <- Writer.tell(Vector("Finished with " + a.show))
    } yield a
    else
      Writer.tell(Vector(s"${a.show} mod ${b.show} = ${(a % b).show}")) >>= { _ =>
        gcdV(b, a % b)
      }
  }

}
