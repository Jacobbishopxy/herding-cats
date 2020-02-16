package com.github.jacobbishopxy.caseStudies.testingAsynchronousCode

import scala.concurrent.Future

/**
 * Created by jacob on 2/16/2020
 */
object AbstractingOverTypeConstructors {

  /**
   * We need to implement two versions of UptimeClient: an asynchronous one
   * for use in production and a synchronous one for use in our unit tests:
   */

  /**
   * Id allows us to abstract over the return types in UptimeClient. Implement this now:
   *
   * • write a trait definition for UptimeClient that accepts a type constructor F[_] as a parameter;
   * • extend it with two traits, RealUptimeClient and TestUptimeClient, that bind F to Future and Id respectively;
   * • write out the method signature for getUptime in each case to verify that it compiles.
   */

  import cats.Id

  trait UptimeClient[F[_]] {
    def getUptime(hostname: String): F[Int]
  }

  trait RealUptimeClient extends UptimeClient[Future] {
    override def getUptime(hostname: String): Future[Int]
  }

  trait TestUptimeClient extends UptimeClient[Id] {
    override def getUptime(hostname: String): Int
  }

  class TestUptimeClient2(hosts: Map[String, Int]) extends UptimeClient[Id] {
    override def getUptime(hostname: String): Id[Int] = hosts.getOrElse(hostname, 0)
  }

}
