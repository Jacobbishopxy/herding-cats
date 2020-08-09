package com.github.jacobbishopxy.catsEffect.dataTypes

import java.io.{BufferedReader, File, FileInputStream, InputStreamReader}

import cats.effect.{ContextShift, IO}
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.atomic.AtomicBoolean

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.io.Source
import scala.util.control.NonFatal
import scala.util.{Failure, Success}


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
   * `IO`可以延迟副作用所以他是一个lazy的数据类型，很多时候会与基础库的`Future`做比较。以下是他们的分类：
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

object IODataType3 {

  /**
   * Describing Effects
   *
   * `IO`是一个强大的抽象，用于描述不同类型的副作用
   */

  /**
   * Pure Values -- IO.pure & IO.unit
   *
   * 你可以提升一个纯值到IO，即产生以及被定义的IO值。
   * `def pure[A](a: A): IO[A] = ???`
   */

  IO.pure(25).flatMap(n => IO(println(s"Number is: $n")))

  /**
   * 很明显`IO.pure`不能延迟副作用，因为它是急加载的。
   *
   * `IO.unit`是一个简化版`IO.pure(())`的别称。
   */

  /**
   * Synchronous Effects -- IO.apply
   *
   * `IO.apply`可能是用的对多的构造器，等同于`Sync[IO].delay`。描述在当前线程和栈上，可被立刻执行的IO操作。
   * `def apply[A](body: => A): IO[A] = ???`
   * 注意函数的入参是“by name”，即它的执行在IO中是被“延迟的”。
   */

  // 下面的例子是从console中读/写，是在JVM之上的同步I/O，所以它的执行是即刻的：
  def putStrLn(value: String): IO[Unit] = IO(println(value))

  val readLn: IO[String] = IO(scala.io.StdIn.readLine())

  // 接着我们可以使用它于console进行纯函数式的交互：
  for {
    _ <- putStrLn("What's your name?")
    n <- readLn
    _ <- putStrLn(s"Hello, $n!")
  } yield ()

  /**
   * Asynchronous Effects -- IO.async & IO.cancelable
   *
   * `IO`可以通过`IO.async`和`IO.cancelable`用于描述异步计算。`IO.async`过程是由`Async#async`原则构造的，可以简单的
   * 描述不可取消的异步过程，它的函数签名如下：
   *
   * `def async[A](k: (Either[Throwable, A] => Unit) => Unit): IO[A] = ???`
   *
   * 该函数的入参是一个回调函数，回调函数的入参是either，右成功`Right(a)`，或左失败`Left(error)`。用户可以触发提供的异步副作用，
   * 然后通过回调函数完成。
   *
   * 列如，你不需要转换Scala的`Future`，因为我们有`IO.fromFuture`，转换的代码也非常直接：
   */
  def convert[A](fa: Future[A])(implicit ec: ExecutionContext): IO[A] =
    IO.async { cb =>
      // This triggers evaluation of the by-name param and of onComplete,
      // so it's OK to have side effects in this callback
      fa.onComplete {
        case Success(a) => cb(Right(a))
        case Failure(e) => cb(Left(e))
      }
    }

  /**
   * Cancelable Processes
   *
   * 在构建可取消的IO任务时，你需要用到`IO.cancelable`构造器。该构造器由`Concurrent#cancelable`原则构造的，
   * 这里是它的函数签名：
   *
   * `def cancelable[A](k: (Either[Throwable, A] => Unit) => IO[Unit]): IO[A] = ???`
   *
   * 与`IO.async`类似，但是注册函数需要提供`IO[Unit]`用于捕获取消逻辑。重要：可取消是打断未完成IO任务，可能释放
   * 任何资源，便于于竞争条件下的防治内存泄漏。
   *
   * 例如，假设我们描述休眠操作，并依赖于Java的`ScheduledExecutorService`，一段时间内延迟一跳：
   */
  def delayedTick(d: FiniteDuration)(implicit sc: ScheduledExecutorService): IO[Unit] =
    IO.cancelable { cb =>
      val r = new Runnable {
        override def run(): Unit = cb(Right(()))
      }
      val f = sc.schedule(r, d.length, d.unit)

      // Returning the cancellation token needed to cancel
      // the scheduling and release resources early
      IO(f.cancel(false)).void
    }

  /**
   * IO.never
   *
   * `IO.never`表示不终止的`IO`，定义在`async`中。一般用于捷径和可复用的参考，签名：
   *
   * `val never: IO[Nothing] = IO.async(_ => ())`
   *
   * 一些情况下用于不终止的环境，如线程竞争。例，我们有这些等同于`IO.race`：
   *
   * `IO.race(lh, IO.never) <-> lh.map(Left(_))`
   * `IO.race(IO.never, rh) <-> rh.map(Right(_))`
   */

  /**
   * Deferred Execution -- IO.suspend
   *
   * `IO.suspend`构造器等同于：
   * `IO.suspend(f) <-> IO(f).flatten`
   *
   * 一般作用于延迟副作用，但同时返回IO也被延迟了。另外也可以用于栈安全模型，尾递归：
   */
  def fib(n: Int, a: Long, b: Long): IO[Long] =
    IO.suspend {
      if (n > 0) fib(n - 1, b, a + b) else IO.pure(a)
    }

  /**
   * 通常来说，像这样的一个函数最终会在JVM上返回栈溢出。但是用`IO.suspend`并且所有循环都用`IO`，它最终是懒加载的，内存消耗恒定。
   * `flatMap`也同样使用，当然`suspend`在这个案例中更好一些。
   *
   * 我们可以用Scala的`@tailrec`装饰器，然而使用`IO`我们可以同样做到插入异步界限：
   */
  def fib2(n: Int, a: Long, b: Long)(implicit cs: ContextShift[IO]): IO[Long] =
    IO.suspend {
      if (n == 0) IO.pure(a) else {
        val next = fib2(n - 1, b, a + b)
        if (n % 100 == 0) cs.shift *> next
        else next
      }
    }
}

object IODataType4 {

  /**
   * Concurrency and Cancellation
   *
   * `IO`可以描述可中断的异步过程。实现细节如下：
   *
   * 不是所有的IO任务是可被取消的。进入取消状态需要在异步边界之后。它需要以下2中情况才可达成：
   *
   * - 构建IO任务时使用`IO.cancelable`,`IO.async`,`IO.asyncF`或者`IO.bracket`
   *
   * - 使用`IO.cancelBoundary`或者`IO.shift`
   *
   * 注意第二种情况是第一种情况的结果，并且所有被包含的这些操作同样是可被取消的。它包括但不限于等待`Mvar.take`,`Mvar.put`和
   * `Deferred.get`。
   *
   * 我们同时也需要注意`flatMap`链当且仅当在一个异步界限之后可以被取消。在一个异步界限之后，取消机制会应用到任何N`faltMap`。
   * N值为硬编码512。
   */
  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  def retryUntilRight[A, B](io: IO[Either[A, B]]): IO[B] =
    io.flatMap {
      case Right(b) => IO.pure(b)
      case Left(_) => retryUntilRight(io)
    }

  // non-terminating IO that is NOT cancelable
  val notCancelable: IO[Int] = retryUntilRight(IO(Left(0)))

  // non-terminating IO that is cancelable because there is an
  // async boundary created by IO.shift before `flatMap` chain
  val cancelable: IO[Int] = IO.shift *> retryUntilRight(IO(Left(0)))

  /**
   * Building cancelable IO tasks
   *
   * 可被取消的`IO`任务可被`IO.cancelable`所构建。`delayedTick`案例用到的是Java的`ScheduledExecutorService`，这里回忆一下：
   */
  def sleep(d: FiniteDuration)(implicit sc: ScheduledExecutorService): IO[Unit] =
    IO.cancelable { cb =>
      val r = new Runnable {
        override def run(): Unit = cb(Right(()))
      }
      val f = sc.schedule(r, d.length, d.unit)
      IO(f.cancel(false)).void
    }

  /**
   * 重要：如果你的任务没有指定取消逻辑，那么这个任务是不可被取消的。以Java的blocking I/O为例:
   */
  def unsafeFileToString(file: File): String = {
    val in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"))

    try {
      // Uninterruptible loop
      val sb = new StringBuilder()
      var hasNext = true
      while (hasNext) {
        hasNext = false
        val line = in.readLine()
        if (line != null) {
          hasNext = true
          sb.append(line)
        }
      }
      sb.toString
    } finally {
      in.close()
    }
  }

  def readFile(file: File)(implicit ec: ExecutionContext): IO[String] =
    IO.async[String] { cb =>
      ec.execute(() => {
        try {
          // Signal completion
          cb(Right(unsafeFileToString(file)))
        } catch {
          case NonFatal(e) =>
            cb(Left(e))
        }
      })
    }

  /**
   * 上述案例显然是不可被取消的。现在我们不用Java的`Thread.interrupt`，因为这是不安全、不可靠的，况且任何`IO`需要针对不同平台的便携性。
   *
   * 但是这里还有许多灵活的地方我们可以改进。我们可以简单的让一个变量转换为`false`，并在`while`循环中观察：
   */
  def unsafeFileToStringPro(file: File, isActive: AtomicBoolean): String = {
    val sc = new StringBuilder
    val linesIterator = Source.fromFile(file).getLines()
    var hasNext = true
    while (hasNext && isActive.get) {
      sc.append(linesIterator.next())
      hasNext = linesIterator.hasNext
    }
    sc.toString
  }

  def readFilePro(file: File)(implicit ec: ExecutionContext): IO[String] =
    IO.cancelable[String] { cb =>
      val isActive = new AtomicBoolean(true)

      ec.execute(() => {
        try {
          // Signal completion
          cb(Right(unsafeFileToStringPro(file, isActive)))
        } catch {
          case NonFatal(e) =>
            cb(Left(e))
        }
      })
      // On cancel, signal it
      IO(isActive.set(false)).void
    }

  /**
   * Gotcha: Cancellation is a Concurrent Action!
   *
   * 通常这并不是显而易见的，无论是从上述例子中，还是你或许会尝试如下的：
   */
  def readLine(in: BufferedReader)(implicit ec: ExecutionContext): IO[String] =
    IO.cancelable[String] { cb =>
      ec.execute(() => cb(
        try Right(in.readLine())
        catch {
          case NonFatal(e) => Left(e)
        }
      ))
      // Cancellation logic is not thread-safe!
      IO(in.close()).void
    }

  /**
   * 这样类似的操作可能在流处理中，通过`IO`（如FS2，Monix等库）抽象I/O区块很有用。
   *
   * 但是上述的操作是错误的，因为`in.close()`是与`in.readLine`并行的，这样会导致抛出异常，并且多数情况下会导致数据污染。这显然是大错特错，
   * 我们希望终端的IO无论它是在做什么，同时还不会造成数据污染。
   *
   * 因此用户需要考虑去处理线程安全。以下是一种方法：
   */
  def readLinePro(in: BufferedReader)(implicit ec: ExecutionContext): IO[String] =
    IO.cancelable[String] { cb =>
      val isActive = new AtomicBoolean(true)
      ec.execute {() =>
        if (isActive.getAndSet(false)) {
          try cb(Right(in.readLine()))
          catch {
            case NonFatal(e) => cb(Left(e))
          }
        }
        // Note there's no else; if cancellation was executed then we don't call
        // the callback; task becoming non-terminating
      }
      // Cancellation logic
      IO {
        // Thread-safe gate
        if (isActive.getAndSet(false)) in.close()
      }.void
    }

  /**
   * 在这个例子中，取消的逻辑本身调用`in.close()`，但是调用是安全的，这归功于我们使用了一个原子的`getAndSet`带来的线程守护。
   */
}


