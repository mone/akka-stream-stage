package com.github.mone

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import org.scalatest.{FlatSpecLike, Matchers, TryValues}

trait BaseSpec extends FlatSpecLike with Matchers with TryValues

abstract class BaseStreamSpec extends TestKit(ActorSystem()) with BaseSpec {
  implicit val materializer: ActorMaterializer = ActorMaterializer()
}