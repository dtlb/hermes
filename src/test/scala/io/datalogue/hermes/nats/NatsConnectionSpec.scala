package io.datalogue.hermes.nats

import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

class NatsConnectionSpec extends WordSpec with Matchers {

  "NatsConnection" should {
    "allows to publish and subscribe to message" in {
      val connection = NatsConnection()

      val latch = new CountDownLatch(1)
      connection.subscribe("topic", msg => {
        val content = new String(msg.getData)
        content should be("Hello World")
        latch.countDown()
      })

      connection.publish("topic", "Hello World".getBytes())

      latch.await(1, TimeUnit.SECONDS) should be(true)

    }

    "allows to publish and receive reply" in {
      val connection = NatsConnection()

      connection.subscribe(
        "msg",
        msg => {
          val content = new String(msg.getData)
          if (content == "What is your name") {
            msg.getConnection.publish(msg.getReplyTo, "John".getBytes())
          }
        }
      )

      val f = connection.send("msg", "What is your name".getBytes)

      val maybeResponse = f.map(
        msg => {
          new String(msg.getData)
        }
      )
      Await.result(maybeResponse, 1.seconds) should be("John")

    }
  }

}

