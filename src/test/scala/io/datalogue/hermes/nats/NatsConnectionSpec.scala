package io.datalogue.hermes.nats

import java.util.UUID
import java.util.concurrent.{CountDownLatch, TimeUnit}

import cats.effect.{ContextShift, IO}
import io.nats.streaming.SubscriptionOptions
import org.scalatest.{Matchers, WordSpec}

class NatsConnectionSpec extends WordSpec with Matchers {

  private val host = "localhost:4222"
  val connection = NatsConnection.connect(host, "test-cluster", "spec").unsafeRunSync()

  "NatsConnection" should {
    "allows to publish and subscribe to message" in {

      val latch = new CountDownLatch(1)
      for {
        _ <- connection.subscribe("topic", msg => {
          val content = new String(msg.getData)
          content should be("Hello World")
          latch.countDown()
        })
        _ <- connection.publish("topic", "Hello World".getBytes())
        assertion = latch.await(1, TimeUnit.SECONDS) should be(true)
      } yield assertion
    }

    "allows to publish and subscribe even old messages using stream" in {
      val topic = s"topic-${UUID.randomUUID().toString}"
      implicit val ioContextShift: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)

      for {
        _ <- connection.publish(topic, "One".getBytes())
        stream = connection.subscribeAsStream(topic)
        _ <- connection.publish(topic, "Two".getBytes())
        _ <- connection.publish(topic, "Three".getBytes())
        l <- stream.take(3).compile.toList
        assertion = l.map(msg => new String(msg.getData)) should be (List("One", "Two", "Three"))
      } yield assertion
    }

    "allows to subscribe again with durableName and read only new messages" in {
      val topic = s"topic-${UUID.randomUUID().toString}"
      implicit val ioContextShift: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)

      def createDurableSubscription(connection: NatsConnection) = {
        val options = new SubscriptionOptions.Builder()
          .durableName("sub_name")
          .manualAcks()
          .deliverAllAvailable()
          .build()
        connection.subscribeAsStream(topic, Some(options))
      }

      (for {
        _ <- connection.publish(topic, "One".getBytes())
        _ <- connection.publish(topic, "Two".getBytes())
        connection1 <- NatsConnection.connect(host, "test-cluster", "durable-spec")
        _ <- createDurableSubscription(connection1).take(1).map(msg => {
          msg.ack()
        }).compile.toList
        _ <- connection1.close()
        _ <- connection.publish(topic, "Three".getBytes())
        connection2 <- NatsConnection.connect(host, "test-cluster", "durable-spec")
        l <- createDurableSubscription(connection2).take(2).compile.toList
        assertion = l.map(msg => new String(msg.getData)) should be (List("Two", "Three"))
      } yield assertion).unsafeRunSync()

    }

    "allows to publish and subscribe only new messages stream" in {
      val topic = s"topic-${UUID.randomUUID().toString}"
      implicit val ioContextShift: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)

      (for {
        _ <- connection.publish(topic, "One".getBytes())

        stream = connection.subscribeAsStream(topic, Some(new SubscriptionOptions.Builder()
          .build()))

        _ <- connection.publish(topic, "Two".getBytes())
        _ <- connection.publish(topic, "Three".getBytes())

        l <- stream.take(2).compile.toList
        assertion = l.map(msg => new String(msg.getData)) should be (List("Two", "Three"))
      } yield assertion).unsafeRunSync()

    }

  }

}

