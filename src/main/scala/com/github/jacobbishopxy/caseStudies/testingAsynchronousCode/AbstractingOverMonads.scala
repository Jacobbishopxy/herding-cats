package com.github.jacobbishopxy.caseStudies.testingAsynchronousCode

/**
 * Created by jacob on 2/16/2020
 */
object AbstractingOverMonads {

  /**
   * Let's turn our attention to  UptimeService. We need to rewrite it to abstract
   * over the two types of UptimeClient. We’ll do this in two stages: first we’ll
   * rewrite the class and method signatures, then the method bodies. Starting
   * with the method signatures:
   *
   * • comment out the body of getTotalUptime (replace it with ??? to make everything compile);
   * • add a type parameter F[_] to UptimeService and pass it on to UptimeClient.
   */

  import cats.Applicative
  import cats.instances.list._ // for Traverse[List]
  import cats.syntax.traverse._
  import cats.syntax.functor._

  import AbstractingOverTypeConstructors.{UptimeClient, TestUptimeClient2}

  class UptimeService[F[_] : Applicative](client: UptimeClient[F]) {
    def getTotalUptime(hostnames: List[String]): F[Int] =
      hostnames.traverse(client.getUptime).map(_.sum)
  }

  def testTotalUptime(): Unit = {
    val hosts = Map("host1" -> 10, "host2" -> 6)
    val client = new TestUptimeClient2(hosts)
    val service = new UptimeService(client)
    val actual = service.getTotalUptime(hosts.keys.toList)
    val expected = hosts.values.sum
    assert(actual == expected)
  }

  testTotalUptime()
}
