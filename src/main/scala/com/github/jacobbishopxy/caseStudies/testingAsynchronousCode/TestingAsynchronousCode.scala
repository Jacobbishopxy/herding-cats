package com.github.jacobbishopxy.caseStudies.testingAsynchronousCode

/**
 * Created by jacob on 2/16/2020
 *
 * We’ll start with a straightforward case study: how to simplify unit tests for asynchronous code by
 * making them synchronous.
 */
object TestingAsynchronousCode {

  /**
   * Let’s return to the example from Chapter 7 where we’re measuring the uptime
   * on a set of servers. We’ll flesh out the code into a more complete structure.
   * There will be two components. The first is an UptimeClient that polls remote
   * servers for their uptime:
   */

  import scala.concurrent.Future

  trait UptimeClient {
    def getUptime(hostname: String): Future[Int]
  }

  /**
   * We’ll also have an UptimeService that maintains a list of servers and allows
   * the user to poll them for their total uptime:
   */

  import cats.instances.future._ // for Applicative
  import cats.instances.list._ // for Traverse
  import cats.syntax.traverse._ // for traverse

  import scala.concurrent.ExecutionContext.Implicits.global

  class UptimeService(client: UptimeClient) {
    def getTotalUptime(hostnames: List[String]): Future[Int] =
      hostnames.traverse(client.getUptime).map(_.sum)
  }

  /**
   * We’ve modelled UptimeClient as a trait because we’re going to want to stub
   * it out in unit tests. For example, we can write a test client that allows us to
   * provide dummy data rather than calling out to actual servers:
   */

  class TestUptimeClient(hosts: Map[String, Int]) extends UptimeClient {
    def getUptime(hostname: String): Future[Int] =
      Future.successful(hosts.getOrElse(hostname, 0))
  }

  /**
   * Now, suppose we’re writing unit tests for UptimeService. We want to test
   * its ability to sum values, regardless of where it is getting them from. Here’s an
   * example:
   */

  //  def testTotalUptime() = {
  //    val hosts = Map("host1" -> 10, "host2" -> 6)
  //    val client = new TestUptimeClient(hosts)
  //    val service = new UptimeService(client)
  //    val actual = service.getTotalUptime(hosts.keys.toList)
  //    val expected = hosts.values.sum
  //    assert(actual == expected)
  //  }

  /**
   * The code does not compile because we’ve made a classic error¹. We forgot
   * that our application code is asynchronous. Our actual result is of type Future[Int] and out
   * expected result is of type Int. We can’t compare them directly!
   */

  /**
   * There are a couple of ways to solve this problem. We could alter our test
   * code to accommodate the asynchronousness. However, there is another alternative.
   * Let’s make our service code synchronous so our test works without modification!
   */

}
