package io.datalogue.hermes.nats

import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.scalatest.{Matchers, WordSpec}

class NatsConnectionSpec extends WordSpec with Matchers {

  private val host = "localhost:4222"
  val connection = NatsConnection(host, "test-cluster", "spec")

  "NatsConnection" should {
    "allows to publish and subscribe to message" in {

      val latch = new CountDownLatch(1)
      connection.subscribe("topic", msg => {
        val content = new String(msg.getData)
        content should be("Hello World")
        latch.countDown()
      })

      connection.publish("topic", "Hello World".getBytes())

      latch.await(1, TimeUnit.SECONDS) should be(true)

    }

  }

}

