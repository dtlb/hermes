package io.datalogue.hermes.nats

import java.util.UUID
import java.util.concurrent.{CountDownLatch, TimeUnit}

import cats.effect.{ContextShift, IO}
import io.nats.streaming.SubscriptionOptions
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

    "allows to publish and subscribe even old messages using stream" in {
      val topic = s"topic-${UUID.randomUUID().toString}"
      implicit val ioContextShift: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)

      connection.publish(topic, "One".getBytes())

      val stream = connection.subscribeAsStream(topic)

      connection.publish(topic, "Two".getBytes())
      connection.publish(topic, "Three".getBytes())

      val l = stream.take(3).compile.toList.unsafeRunSync()

      l.map(msg => new String(msg.getData)) should be (List("One", "Two", "Three"))
    }

    "allows to subscribe again with durableName and read only new messages" in {
      val topic = s"topic-${UUID.randomUUID().toString}"
      implicit val ioContextShift: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)

      connection.publish(topic, "One".getBytes())
      connection.publish(topic, "Two".getBytes())
      def createDurableSubscription(connection: NatsConnection) = {
        val options = new SubscriptionOptions.Builder()
          .durableName("sub_name")
          .manualAcks()
          .deliverAllAvailable()
          .build()
        connection.subscribeAsStream(topic, Some(options))
      }

      val connection1 = NatsConnection(host, "test-cluster", "durable-spec")
      createDurableSubscription(connection1).take(1).map(msg => {
        msg.ack()
      }).compile.toList.unsafeRunSync()

      connection1.close()
      connection.publish(topic, "Three".getBytes())

      val connection2 = NatsConnection(host, "test-cluster", "durable-spec")

      val l = createDurableSubscription(connection2).take(2).compile.toList.unsafeRunSync()

      l.map(msg => new String(msg.getData)) should be (List("Two", "Three"))
    }

    "allows to publish and subscribe only new messages stream" in {
      val topic = s"topic-${UUID.randomUUID().toString}"
      implicit val ioContextShift: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)

      connection.publish(topic, "One".getBytes())

      val stream = connection.subscribeAsStream(topic, Some(new SubscriptionOptions.Builder()
        .build()))

      connection.publish(topic, "Two".getBytes())
      connection.publish(topic, "Three".getBytes())

      val l = stream.take(2).compile.toList.unsafeRunSync()

      l.map(msg => new String(msg.getData)) should be (List("Two", "Three"))
    }

  }

}

