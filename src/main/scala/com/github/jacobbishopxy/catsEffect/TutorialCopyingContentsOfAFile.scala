package com.github.jacobbishopxy.catsEffect

import cats.effect._
import cats.effect.concurrent.Semaphore
import cats.effect.syntax.all._
import cats.implicits._
import java.io._

/**
 * Copying contents of a file - safely handling resources
 *
 * Origin from:
 * https://typelevel.org/cats-effect/tutorial/tutorial.html
 */
object TutorialCopyingContentsOfAFile {

  /**
   * 我们的目标是写一个拷贝文件的程序。 首先我们需要创建一个能承载该任务的函数，然后我们再写一个能在shell里执行任务的程序。
   *
   * 由于是函数式，唤醒该函数不应该拷贝任何东西，而是返回一个IO实例。该实例封装了所有副作用（打开/关闭文件，读/写内容）以确保纯函数性。
   * 只有当IO实例被执行的时侯，所有副作用才会起作用。在我们的实现中，IO实例将返回执行时拷贝字节的总数。当然异常是会出现的，但是当我们
   * 处理任何IO时，他们都应该被嵌入IO实例。简言之，没有异常会在IO外被抛出，而且也不需要try，因为IO执行会失败，IO实例会携带错误返回。
   */
  def copyPrototype(origin: File, destination: File): IO[Long] = ???

  /**
   * Acquiring and releasing `Resource`s
   *
   * 我们考虑用stream作为副作用的过程，我们需要封装这些过程至我们的IO实例中。因此，我们需要用到cats-effect的`Resource`，来确保有序的
   * 创建create，使用use和输出release资源。
   */
  def inputStream(f: File): Resource[IO, FileInputStream] =
    Resource.make {
      // build
      IO(new FileInputStream(f))
    } { inStream =>
      // release
      IO(inStream.close()).handleErrorWith(_ => IO.unit)
    }

  def outputStream(f: File): Resource[IO, FileOutputStream] =
    Resource.make {
      // build
      IO(new FileOutputStream(f))
    } { outStream =>
      // release
      IO(outStream.close()).handleErrorWith(_ => IO.unit)
    }

  def inputOutputStream(in: File, out: File): Resource[IO, (InputStream, OutputStream)] =
    for {
      inStream <- inputStream(in)
      outStream <- outputStream(out)
    } yield (inStream, outStream)

  /**
   * 我们希望再使用streams时，确保他们无论何种情况都能被一次性被关闭。这也是为什么我们使用`Resource`了在`inputStream`和
   * `outputStream`的函数上，他们分别返回一个封装了开启关闭各自stream的`Resource`。`inputOutputStream`封装了两个独立的
   * `Resource`实例，只有当两个stream同时成功时它才变得可用。可以看的出来`Resource`实例时可以被for表达式所结合，因为他们都
   * 实现了`flatMap`。当输出这些资源的时候，我们需要关注子`Resource`可能出现的异常，同时我们也需要关注主`Resource`的异常。
   * 例如子`Resource`中的`.handleErrorWith`，我们吞掉了这些错误，而现实情况他们至少需要被Log出来。
   *
   * 除此之外，我们也可以用`Resource.fromAutoCloseable`来定义我们的资源，这种情况下的`Resource`实例实现了`java.lang.AutoCloseable`
   * 的接口，因此不再需要定义资源是如何被输出的。
   */
  def inputStreamAutoCloseable(f: File): Resource[IO, FileInputStream] =
    Resource.fromAutoCloseable(IO(new FileInputStream(f)))

  /**
   * 这种情况下虽然代码简单，但是我们不能控制结束操作时时所抛出的异常。因此，使用`Resource.make`允许我们
   * 在资源输出时期控制异常。
   *
   * 现在回到`copy`函数：
   * 这里的新函数`transfer`实现的时真实的拷贝数据。当他们不被需要时，无论`transfer`的结果是失败或成功，两个stream都会被关闭。
   * 也就是说，如果在打开output文件时出现异常，input stream将被关闭 。
   */
  def transfer(origin: InputStream, destination: OutputStream): IO[Long] = ???

  def copy(origin: File, destination: File): IO[Long] =
    inputOutputStream(origin, destination).use { case (in, out) =>
      transfer(in, out)
    }

  /**
   * What about `bracket`?
   *
   * 如果你熟悉cats-effect中的`bracket`，可能会疑惑为什么我们不使用它而使用`Resource`（因为`Resource`是基于`bracket`构建的）。
   * 那么我们现在来看看`bracket`。
   *
   * 使用`bracket`有三个阶段：resource acquisition, usage, release。每个阶段都是由IO实例所定义。`bracket`的一个基础的属性是：
   * 无论使用阶段usage的成功与否，输出阶段release都将被执行。在我们的案例中，获取阶段acquisition我们创建了streams，使用阶段usage
   * 我们将拷贝内容，最终在输出阶段release我们关闭streams。
   *
   * 新的copy函数更加复杂，虽然我们不再需要inputOutputStreams函数，但是我们没了有处理异常的功能。使用`bracket`时，如果在第一个阶段
   * 出现了异常，那么最后阶段将不会被执行。在下面的代码中，原始文件和目标文件将会依次打开（tupled识别两个IO实例为单独的一个），那么当
   * 第一个文件正常而第二个文件异常时，会出现什么情况？我们的第一个stream将不会被关闭！解决这个问题我们需要第一个stream使用`bracket`，
   * 第二个stream在第一个`bracket`中再使用一个`bracket`。这也就意味着通过`flatMap`处理`Resource`的实例。因此在处理多个资源同时打开
   * 的问题时，`Resource`更加符合我们的要求。
   *
   * 使用`bracket`时,我们是这样定义copy函数:
   */
  def copyBracket(origin: File, destination: File): IO[Long] = {
    val inIO = IO(new FileInputStream(origin))
    val outIO = IO(new FileOutputStream(destination))

    (inIO, outIO)
      .tupled
      .bracket { case (in, out) =>
        transfer(in, out)
      } {
        case (in, out) =>
          (IO(in.close()), IO(out.close()))
            .tupled
            .handleErrorWith(_ => IO.unit).void
      }
  }

  /**
   * Copying data
   *
   * 现在我们来完成`transfer`函数。这个函数需要定义这样一个循环：每个迭代中从input stream读数据进入buffer，然后把buffer
   * 中的内容写入output stream。同时，这个循环将保持一个计数器用于记录多少字节被转换。为了同样的buffer可以被复用，我们需要定义
   * 该循环在主循环外部，并且让转换数据的工作交给另一个`transmit`的函数使用该循环。
   *
   * 我们来看看transmit函数，input和output过程都被封装在IO里。IO是一个monad，我们可以用for表达式序列化的创建其他的IO。在for
   * 表达式中只要read（）没有返回负值便不会触发stream结束。`>>`为cats的表达符，用于序列化的连接两个操作，并且第一个操作的输出
   * 不被第二个操作所需要（等同于`first.flatMap(_ => second)`）。在以下的代码中，即意味着每当一个写的操作结束时我们以递归的
   * 方式再次执行`transmit`，由于IO是栈安全的，我们不需要考虑栈溢出的情况。每一次迭代我们给计数器acc增加读取时的字节数。
   *
   * 我们现在有了copy的全貌。当任何异常发生在`transfer`时，所有的stream将会自动被`Resource`关闭。这里我们要注意一些别的问题：
   * IO实例是可以用被canceled的！结束IO是不能被忽视的，因为在cats-effect中这是一个重要的特性。
   */
  def transmit(origin: InputStream, destination: OutputStream, buffer: Array[Byte], acc: Long): IO[Long] =
    for {
      amount <- IO(origin.read(buffer, 0, buffer.length))
      count <- {
        if (amount > -1)
          IO(destination.write(buffer, 0, amount)) >> transmit(origin, destination, buffer, acc + amount)
        else {
          // End of read stream reached (by jave.io.InputStream contract), nothing to write
          IO.pure(acc)
        }
      }
    } yield count // returns the actual amount of bytes transmitted

  def transferC(origin: InputStream, destination: OutputStream): IO[Long] =
    for {
      // Allocated only when the IO is evaluated
      buffer <- IO(new Array[Byte](1024 * 10))
      total <- transmit(origin, destination, buffer, 0L)
    } yield total

}


object TutorialCopyingContentsOfAFile1 {

  /**
   * Dealing with cancellation
   *
   * Cancellation很强大并且是一个不可忽视的特性。在cats-effect中，一些IO实例是可以被取消的，这就意味着他们的执行会被中止。
   * 一个小心翼翼的程序员，是会将IO任务置于cancellation下的，例如执行清除任务。在接下来的Fibers are not threads中，我们将会
   * 看到一个IO是如何真正的被取消。但是现在只需关注在copy方法返回的IO中，我们可以在任意时刻发送一个cancellation请求。
   *
   * 由`Resource.use`所创建的IO们是可以被取消的。取消将会触发资源关闭的代码。在我们的例子中，即关闭两个stream。但是如果是streams
   * 在被使用时发生的取消请求呢？这会导致数据污染，即一些线程在写数据同时别的线程在关闭他们。
   *
   * 为了防止数据污染的情况发生，我们需要用到并发控制机制来确保，在transfer返回的IO被执行时时，没有stream会被关闭。cats-effect
   * 提供了若干结构用于控制并发，这个案例中我们需要用到一个semaphore。一个semaphore有一定量的许可，如果没有许可可用，它的方法
   * `.acquire`会“阻塞”，直到`release`被同一个semaphore调用。值得注意的是，这里没有线程是真正的被“阻塞”，一个线程发现`.acquire`
   * 被调用将会立刻被cats-effect回收。当`release`方法被唤醒，cats-effect会寻找一些可用的线程用于恢复执行（`.acquire`后的代码）.
   *
   * 我们将会使用一个带有一个许可的semaphore。`.withPermit`方法需要一个许可，用于运行IO并释放许可。同样的我们也可以使用`.acquire`
   * 然后在这个semaphore显示的执行`.release`。这里使用`.withPermit`是因为更加典型，并且可以确保即使运行错误，许可也会被释放。
   *
   * 在调用`transfer`前，我们需要semaphore，它不会释放直到`transfer`完成。`use`确保semaphore可以在任何情况释放，无论`transfer`
   * 的结果（成功，失败，或取消）。在`Resource`实例的`release`部分，现在被同一个semaphore阻塞，我们可以确认streams只会在`transfer`
   * 结束后关闭，例如我们实现了相互独立的`transfer`执行和资源释放。函数签名中，需要一个隐式的`Concurrent`实例用于创建semaphore实例。
   *
   * 现在copy返回的IO是可被取消的了，`transfer`返回的IO实例则不是。尝试取消它不会带来任何影响，并且IO会持续运行直至整个文件被拷贝！
   * 现实世界中，你大概想要你的函数可以被取消的，在Building cancelable IO task中的IO文档会解释如何创建可被取消的IO
   * （不同于`Resource.use`）。
   *
   * 这就是所有的内容了！我们现在可以写一个用copy函数的程序了。
   */
  def transfer(origin: InputStream, destination: OutputStream): IO[Long] = ???

  def inputStream(f: File, guard: Semaphore[IO]): Resource[IO, FileInputStream] =
    Resource.make {
      IO(new FileInputStream(f))
    } { inStream =>
      guard.withPermit {
        IO(inStream.close()).handleErrorWith(_ => IO.unit)
      }
    }

  def outputStream(f: File, guard: Semaphore[IO]): Resource[IO, FileOutputStream] =
    Resource.make {
      IO(new FileOutputStream(f))
    } { outStream =>
      guard.withPermit {
        IO(outStream.close()).handleErrorWith(_ => IO.unit)
      }
    }

  def inputOutputStreams(in: File, out: File, guard: Semaphore[IO]): Resource[IO, (InputStream, OutputStream)] =
    for {
      inStream <- inputStream(in, guard)
      outStream <- outputStream(out, guard)
    } yield (inStream, outStream)

  def copy(origin: File, destination: File)(implicit concurrent: Concurrent[IO]): IO[Long] =
    for {
      guard <- Semaphore[IO](1)
      count <- inputOutputStreams(origin, destination, guard).use { case (in, out) =>
        guard.withPermit(transfer(in, out))
      }
    } yield count
}


object Main extends IOApp {

  /**
   * `IOApp` for our final program
   *
   * 这个程序只接受两个参数：原始文件和目标文件的名字。我们使用`IOApp`，因为它可以让我们的主函数保持纯函数。
   *
   * `IOApp`是一个类似与Scala的`App`，不同之处在于我们使用纯函数运行。当我们运行run时2，任何的打断（比如Ctrl+c）将被视为取消
   * 正在运行的IO。同样的`IOApp`提供隐式`Timer[IO]`和`ContextShift[IO]`实例（暂未涉及）。`ContextShift[IO]`提供一个
   * `Concurrent[IO]`在域中，用于copy函数。
   */
  def copy(origin: File, destination: File): IO[Long] = ???

  override def run(args: List[String]): IO[ExitCode] =
    for {
      _ <-
        if (args.length < 2) IO.raiseError(new IllegalArgumentException("Need origin and destination files"))
        else IO.unit
      orig = new File(args.head)
      dest = new File(args(1))
      count <- copy(orig, dest)
      _ <- IO(println(s"$count bytes copied from ${orig.getPath} to ${dest.getPath}"))
    } yield ExitCode.Success

}


object PolymorphicCatsEffect {

  /**
   * 我们还需要注意IO的一个重要特征。IO可以封装副作用，但是定义并行的async和cancelable的IO实例依赖于已存在的`Concurrent[IO]`实例。
   * `Concurrent[F[_]]`是一个typeclass，这里的F携带副作用，这个typeclass拥有并行的取消或开始F副作用的能力。`Concurrent`继承了
   * typeclass `Async[F[_]]`，因此允许你定义同步/异步计算。`Async[F[_]]`继承了typeclass `Sync[F[_]]`，所以可以延迟副作用的
   * 执行。
   *
   * 那么我们是否能用`F[_]: Sync`来代替 `IO`呢？是的，并且在生产代码中这是推荐的。看一下我们将要定义一个多态版本的`transfer`函数，
   * 即使用`Sync[F[_]]`实例的`delay`和`pure`方法，替换所有用到`IO`的地方。
   */
  def transmit[F[_] : Sync](origin: InputStream,
                            destination: OutputStream,
                            buffer: Array[Byte],
                            acc: Long): F[Long] =
    for {
      amount <- Sync[F].delay(origin.read(buffer, 0, buffer.length))
      count <-
        if (amount > -1)
          Sync[F].delay(destination.write(buffer, 0, amount)) >>
            transmit(origin, destination, buffer, acc + amount)
        else
          Sync[F].pure(acc)
    } yield count

  /**
   * 我们可以使用大部分之前的代码，但是copy函数我们需要一个完整的`Concurrent[F]`实例，这是因为它是被`Semaphore`实例所需要的。
   *
   * 只有在你的main函数中我们才会为F设置IO。当然了，一个`Concurrent[IO]`实例需要在作用域中，但是该实例是显然可以由`IOApp`提供，
   * 所以我们不需要关心它。
   *
   * 多态的代码拥用更少的约束，因为函数们不会被IO绑定，并且适用于任何`F[_]`只要有一个typeclass的实例在作用域中需要(`Sync[F[_]]`,
   * `Concurrent[F[_]]`等)。所需的typeclass将会由我们的代码提供。列，如果副作用的执行需要被取消，我们就用上`Concurrent[F[_]]`，
   * 同时可以更加方便的使用任何类型的F。
   */
  def transfer[F[_] : Sync](origin: InputStream,
                            destination: OutputStream): F[Long] = ???

  def inputOutputStreams[F[_] : Sync](in: File,
                                      out: File,
                                      guard: Semaphore[F]): Resource[F, (InputStream, OutputStream)] = ???

  def copy[F[_] : Concurrent](origin: File, destination: File): F[Long] =
    for {
      guard <- Semaphore[F](1)
      count <- inputOutputStreams(origin, destination, guard).use { case (in, out) =>
        guard.withPermit(transfer(in, out))
      }
    } yield count

}

