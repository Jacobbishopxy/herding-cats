package com.github.jacobbishopxy.theory.monadTransformers

/**
 * Created by jacob on 2/6/2020
 */
object TransformAndRollOut {


  import cats.data.EitherT
  import cats.instances.future._

  import scala.concurrent.{Future, Await}
  import scala.concurrent.duration._
  import scala.concurrent.ExecutionContext.Implicits.global


  /**
   * Now test the code by implementing `getPowerLevel` to retrieve data from a set of imaginary allies.
   * Here's the data we'll use:
   */
  val powerLevels = Map(
    "Jazz" -> 6,
    "Bumblebee" -> 8,
    "Hot Rod" -> 10,
  )

  /**
   * The Autobots, well-known robots in disguise, frequently send messages during battle requesting the
   * power levels of their team mates. This helps them coordinate strategies and launch devastating attacks.
   * The message sending method looks like this:
   *
   * `def getPowerLevel(ally: String): Response[Int] = ???`
   *
   * If an Autobot isn't in the `powerLevels` map, return an error message reporting that they were
   * unreachable. Include the name in the message for good effect.
   *
   * Transmissions take time in Earth's viscous atmosphere, and messages are occasionally lost due to
   * satellite malfunction or sabotage by pesky Decepticons. Responses are therefore represented as a
   * stack of monads:
   *
   * `type Response[A] = Future[Either[String, A]]`
   *
   * Optimus Prime is getting tired of the nested for comprehensions in his neural matrix. Help him by
   * rewriting `Response` using a monad transformer.
   */

  type Response[A] = EitherT[Future, String, A]

  def getPowerLevel(ally: String): Response[Int] =
    powerLevels.get(ally) match {
      case Some(v) => EitherT.right(Future(v))
      case None => EitherT.left(Future(s"$ally unreachable"))
    }

  /**
   * Two autobots can perform a special move if their combined power level is greater than 15. Write a
   * second method, `canSpecialMove`, that accepts the names of two allies and checks whether a special
   * move is possible. If either ally is unavailable, fail with an appropriate error message:
   *
   * `def canSpecialMove(ally1: String, ally2: String): Response[Boolean] = ???`
   */

  def canSpecialMove(ally1: String, ally2: String): Response[Boolean] =
    for {
      power1 <- getPowerLevel(ally1)
      power2 <- getPowerLevel(ally2)
    } yield (power1 + power2) > 15

  /**
   * Finally, write a method `tacticalReport` that takes two ally names and prints a message saying whether
   * they can perform a special move:
   *
   * `def tacticalReport(ally1: String, ally2: String): String = ???`
   */

  /**
   * Hint:
   *
   * You should be able to use report as follows:
   * `
   * tacticalReport("Jazz", "Bumblebee")
   * // res28: String = Jazz and Bumblebee need a recharge.
   * tacticalReport("Bumblebee", "Hot Rod")
   * // res29: String = Bumblebee and Hot Rod are ready to roll out!
   * tacticalReport("Jazz", "Ironhide")
   * // res30: String = Comes error: Ironhide unreachable
   * `
   */

  def tacticalReport(ally1: String, ally2: String): String = {
    val stack = canSpecialMove(ally1, ally2).value

    Await.result(stack, 1.second) match {
      case Left(msg) => s"Comes error: $msg"
      case Right(true) => s"$ally1 and $ally2 are ready to roll out!"
      case Right(false) => s"ally1 and $ally2 need a recharge."
    }
  }

  // test
  tacticalReport("Jazz", "Bumblebee")
  tacticalReport("Bumblebee", "Hot Rod")
  tacticalReport("Jazz", "Ironhide")
}

