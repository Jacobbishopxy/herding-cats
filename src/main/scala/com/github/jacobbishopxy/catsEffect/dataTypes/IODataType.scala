package com.github.jacobbishopxy.catsEffect.dataTypes

import cats.effect.IO


/**
 * Cats Effect Data Type IO
 *
 * A data type for encoding side effects as pure values, capable of expressing both synchronous and
 * asynchronous computations.
 *
 * Origin from:
 * https://typelevel.org/cats-effect/datatypes/io.html
 */
object IODataType {

  /**
   * Introduction
   *
   * 当`IO[A]`被执行后，返回一个代表副作用的A值。
   *
   * `IO`值是纯的，不可变的值，能被函数式编程运用。一个IO的数据结构是一个关于副作用计算的描述。
   *
   * `IO`可以用于描述同步或异步的计算：
   * 1. 执行后只返回一个结果
   * 2. 可以结束于成功或失败，在flatMap链上有短路失败性质（IO实现了`MonadError`）
   * 3. 可以被取消，但是需要用户提供取消逻辑
   *
   * 带副作用的结果是不会被记忆的，也就是说不会有内存泄漏的风险，同样的，单个副作用可以被透明的执行多次。
   */
  val ioa: IO[Unit] = IO {
    println("hey!")
  }

  val program: IO[Unit] =
    for {
      _ <- ioa
      _ <- ioa
    } yield ()

  /**
   * 打印两次"hey!"，副作用在monadic链中被再次执行
   */
  program.unsafeRunSync()
}

object IODataType2 {

  /**
   * On Referential Transparency and Lazy Evaluation
   *
   * `IO`可以暂停副作用所以他是一个lazy的数据类型，很多时候会与基础库的`Future`做比较。以下是他们的分类：
   *
   * ......|Eager                  |Lazy
   * 同步   |A                      |() => A
   * ......|                       |`Eval[A]`
   * 异步   |(A => Unit) => Unit    |() => (A => Unit) => Unit
   * ......|`Future[A]`            |`IO[A]`
   *
   * `IO`数据类型保持referential transparency，也就是说即便在处理副作用时，它也是lazy执行的。作为急加载的Scala，它们的区别便在于
   * 一个是结果和一个是函数产生。
   *
   * 于`Future`相同的是，`IO`可以用于异步的过程。但是由于纯性质和懒加载，`IO`可以被认为是特别的（在最终步骤执行），在执行期间有更多的
   * 控制以及可预测性。例如，处理序列化VS并行，处理创建多个`IO`或是处理应对失败。
   *
   * 考虑以下案例：
   */

  //  for {
  //    _ <- addToGauge(32)
  //    _ <- addToGauge(32)
  //  } yield ()

  def addToGauge(d: Int): IO[Int] = ???

  // 如果我们有referential transparency，我们可以重写为：
  val task: IO[Int] = addToGauge(32)

  for {
    _ <- task
    _ <- task
  } yield ()
  // `Future`此时不能胜任，但是`IO`可以，并且它是函数式的

  /**
   * Stock Safety
   *
   * 因为`flatMap`计算，`IO`是可递归的。因此不需要担心栈溢出。
   */
  def fib(n: Int, a: Long = 0, b: Long = 1): IO[Long] =
    IO(a + b).flatMap { b2 =>
      if (n > 0) fib(n - 1, b, b2)
      else IO.pure(a)
    }
}

