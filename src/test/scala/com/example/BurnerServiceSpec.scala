package com.example

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._

class BurnerServiceSpec extends Specification with Specs2RouteTest with BurnerService {
  def actorRefFactory = system

  "BurnerService" should {
  }
}
