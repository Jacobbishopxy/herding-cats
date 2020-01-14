package com.github.jacobbishopxy.herdingCats.day0


/**
 * Created by Jacob Xie on 1/14/2020
 *
 * 3. FoldLeft
 */
object S3 extends App {

  import S2.{Monoid, multiMonoid}

  // generalize on foldLeft operation
  object FoldLeftList {
    def foldLeft[A, B](xs: List[A], b: B, f: (B, A) => B): B =
      xs.foldLeft(b)(f)
  }

  //def sum[A: Monoid](xs: List[A]): A = {
  //  val m = implicitly[Monoid[A]]
  //  FoldLeftList.foldLeft(xs, m.mZero, m.mAppend)
  //}

  //println(sum(List(1, 2, 3, 4)))
  //println(sum(List("a", "b", "c")))
  //println(sum(List(1, 2, 3, 4))(multiMonoid))


  // now we can apply the same abstraction to pull out FoldLeft type class
  trait FoldLeft[F[_]] {
    def foldLeft[A, B](xs: F[A], b: B, f: (B, A) => B): B
  }

  object FoldLeft {
    implicit val FoldLeftList: FoldLeft[List] = new FoldLeft[List] {
      override def foldLeft[A, B](xs: List[A], b: B, f: (B, A) => B): B = xs.foldLeft(b)(f)
    }
  }

  def sum[M[_]: FoldLeft, A: Monoid](xs: M[A]): A = {
    val m = implicitly[Monoid[A]]
    val fl = implicitly[FoldLeft[M]]
    fl.foldLeft(xs, m.mZero, m.mAppend)
  }

  //println(sum(List(1, 2, 3, 4)))
  //println(sum(List("a", "b", "c")))

}
