

/**
 * Created by Jacob Xie on 2/21/2020
 */
object KleisliArrows extends App {

  /**
   * 首先我们来看看，Scala通常的函数组合功能：
   * 1. compose
   * 2. andThen
   */

  def mul2: Int => Int = _ * 2
  def power2: Int => Double = Math.pow(_, 2)
  def doubleToInt: Double => Int = _.toInt
  def intToString: Int => String = _.toString

  val pipeline: Int => String = intToString compose mul2 compose doubleToInt compose power2

  println(s"x0: ${pipeline(3)}") // "18"

  /**
   * 那么遇到Monadic函数会是什么样的情况呢？
   */

  def stringToNonEmptyString: String => Option[String] = value =>
    if (value.nonEmpty) Option(value) else None

  def stringToNumber: String => Option[Int] = value =>
    if (value.matches("-?[0-9]+")) Option(value.toInt) else None

  /**
   * 观察一下函数签名，第一个是`String => Option[String]`，第二个是`String => Option[Int]`。
   * 函数衔接部分的类型不一样，都不用编译我们就知道这不可能组合起来。
   * 如果不用flatMap我们就要对第一个函数的返回值手动解包，判断情况再输入到第二个函数得出结果。
   */

  val x1 = Option("1000").flatMap(stringToNonEmptyString).flatMap(stringToNumber)
  println(s"x1: $x1")

  val x2 = Option("Sam").flatMap(stringToNonEmptyString).flatMap(stringToNumber)
  println(s"x2: $x2")

  /**
   * 虽然Scala标准库已经实现了Option是一个Monad，我们还是试试Cats
   */

  import cats.instances.option._
  import cats.syntax.option._
  import cats.syntax.flatMap._

  val x3 = "1000".some >>= stringToNonEmptyString >>= stringToNumber
  println(s"x3: $x3")


  val x4 = "Sam".some >>= stringToNonEmptyString >>= stringToNumber
  println(s"x4: $x4")

  /**
   * 拓展一下，如何运用for表达式进行sequencing computation。
   * 关于sequencing computation是否可以理解为有依赖的连续计算？
   */

  val x5 = for {
    x <- stringToNonEmptyString("1000")
    y <- stringToNumber(x)
  } yield y
  println(s"x5: $x5")

  /**
   * 再拓展一下，我们试试Kleisli这个由Cats实现的数据类型（Kleisli Category）
   */

  import cats.data.Kleisli

  val stringToNonEmptyStringK = Kleisli(stringToNonEmptyString)
  val stringToNumberK = Kleisli(stringToNumber)

  val pipelineK: Kleisli[Option, String, Int] = stringToNumberK compose stringToNonEmptyStringK

  println(s"x6: ${pipelineK("1000")}")
  println(s"x7: ${pipelineK("Sam")}")

}


