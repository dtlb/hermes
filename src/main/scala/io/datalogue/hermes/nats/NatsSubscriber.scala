package io.datalogue.hermes.nats

import java.time.Duration

import cats.effect.IO
import io.nats.streaming.{Options, StreamingConnection, StreamingConnectionFactory}

object NatsSubscriber {
  def apply(url: String, clusterId: String, clientId: String): NatsSubscriber = {
    val opts: Options = new Options.Builder()
      .clientId(clientId)
      .clusterId(clusterId)
      .natsUrl(url)
      .build()
    val con = new StreamingConnectionFactory(opts).createConnection()
    new NatsSubscriber(con)
  }
}

class NatsSubscriber(con: StreamingConnection) {
  private val maxDuration = Duration.ofDays(1000)
  def subscribe(topic: String): fs2.Stream[IO, io.nats.client.Message] = {
    val c = con.getNatsConnection
    val subscription = c.subscribe(topic)
    c.flush(Duration.ofSeconds(1))
    val s = fs2.Stream.repeatEval(IO.delay {
      subscription.nextMessage(maxDuration)
    })
    // TOOD unsubscribe in case of problem
    s

  }

}
