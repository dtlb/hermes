package io.datalogue.hermes.nats

import io.nats.streaming.{Options, StreamingConnectionFactory}
import org.scalatest.{Matchers, WordSpec}

class NatsSubscriberSpec extends WordSpec with Matchers {

  private val host = "localhost:4222"

  val opts: Options = new Options.Builder()
    .clientId("consumer")
    .clusterId("test-cluster")
    .natsUrl(host)
    .build()
  val con = new StreamingConnectionFactory(opts).createConnection()

  "NatsSubscriber" should {
    "allows to subscribe to topic" in {

      val oldCon = con.getNatsConnection
      oldCon.publish("topic", "First".getBytes())

      val subscriber = NatsSubscriber(host, "test-cluster", "consumer")
      val stream = subscriber.subscribe("topic")
      oldCon.publish("topic", "Second".getBytes())
      val result = stream.take(2).compile.toList.unsafeRunSync()

      result.collect {
        case msg if msg != null => new String(msg.getData)
      } should be (List("Second"))

    }

  }

}

