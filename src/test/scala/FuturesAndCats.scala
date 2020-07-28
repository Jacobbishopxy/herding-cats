import cats._
import cats.implicits._
import cats.data._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by Jacob on 7/28/2020
 *
 * original by:
 * https://gist.github.com/rtitle/f73d35e79a2f95871bca27d24be3a805
 */
object FuturesAndCats extends App {

  // 准备4个future函数，其中2和3为异常函数

  val future1: Future[Int] = Future {
    println("future 1 is executing")
    1
  }

  val future2: Future[Int] = Future {
    println("future 2 is executing")
    throw new Exception("Future 2 failed")
  }

  val future3: Future[Int] = Future {
    println("future 3 is executing")
    throw new Exception("Future 3 failed")
  }

  val future4: Future[Int] = Future {
    println("future 4 is executing")
    4
  }

  // 如果对这4个future函数使用flatMap，那么将会在第二个函数中断并抛出异常
  val res1: Future[Int] = for {
    f1 <- future1
    f2 <- future2
    f3 <- future3
    f4 <- future4
  } yield f1 + f2 + f3 + f4

  // Await.result(res1, 1.second)

  // 现在我们可以为每个future函数做一个错误恢复的措施，用于解决异常状态

  def recovery: PartialFunction[Throwable, Int] = {
    case _ => 0
  }

  val res2 = for {
    f1 <- future1.recover(recovery)
    f2 <- future2.recover(recovery)
    f3 <- future3.recover(recovery)
    f4 <- future4.recover(recovery)
  } yield f1 + f2 + f3 + f4

  // 然而这样却丢失了异常信息
  // Await.result(res2, 1.second)

  // 现在轮到Cats来拯救我们了！

  // Cats拥有Apply使得我们可以并行map，并且一次性获得结果
  val res3: Future[Int] = (future1, future2, future3, future4).mapN(_ + _ + _ + _)
  // 等同于
  val res3p: Future[Int] = Apply[Future].map4(future1, future2, future3, future4)(_ + _ + _ + _)

  // Await.result(res3, 1.second)

  // 不同于for表达式，所有的future函数并行运行；与for表达式一样的是，异常还是会在第一时间抛出，即future2时抛出

  // 现在我们来用Cats的Validated[A, B]这个数据类型改进一下。Validated类似于Either[A, B]，即结果只能是A或B，
  // 但是不一样的地方在于，Either是短路型的行为，与for表达式类似，即立刻抛出异常。Validated则是错误累积型的行为。
  // 这里值得注意ValidatedNel[A, B]是Validated[NonEmptyList[A], B]的别称。Cats提供这个别称是因为错误累积
  // 在一个NonEmptyList是一种常用模式。
  // 现在我们就把Future[A]转化成Future[ValidatedNel[Throwable, A]]:

  implicit class EnrichedFuture[A](future: Future[A]) {
    def toValidateNel: Future[ValidatedNel[Throwable, A]] =
      future.map(Validated.valid).recover { case e =>
        Validated.invalidNel(e)
      }
  }

  val res4: Future[ValidatedNel[Throwable, Int]] =
    (future1.toValidateNel, future2.toValidateNel, future3.toValidateNel, future4.toValidateNel)
      .mapN(_ |+| _ |+| _ |+| _)

  // Await.result(res4, 1.second)

  // 现在用Traverse这个typeclass来获取最终结果：
  val res5: Future[List[ValidatedNel[Throwable, Int]]] =
    List(future1, future2, future3, future4).traverse(_.toValidateNel)

  Await.result(res5, 1.second)
}
