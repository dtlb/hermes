package io.datalogue.hermes.nats

import java.time.Duration

import cats.effect.IO
import io.nats.streaming.{Options, StreamingConnection, StreamingConnectionFactory}
import org.log4s.getLogger

object NatsStreamSubscriber {

  private[NatsStreamSubscriber] val log = getLogger

  def apply(url: String, clusterId: String, clientId: String): NatsStreamSubscriber = {
    val opts: Options = new Options.Builder()
      .clientId(clientId)
      .clusterId(clusterId)
      .natsUrl(url)
      .build()
    val con = new StreamingConnectionFactory(opts).createConnection()
    new NatsStreamSubscriber(con)
  }
}

case class StreamSubscriberOptions(
  maxDuration: Duration = Duration.ofMinutes(1)
)

class NatsStreamSubscriber(con: StreamingConnection, options: Option[StreamSubscriberOptions] = None) {

  def subscribe(topic: String): fs2.Stream[IO, io.nats.client.Message] = {
    val c = con.getNatsConnection
    val subscription = c.subscribe(topic)
    c.flush(Duration.ofSeconds(1))
    fs2.Stream
      .repeatEval(IO.delay {
        subscription.nextMessage(options.map(_.maxDuration).getOrElse(StreamSubscriberOptions().maxDuration))
      })
      .collect {
        case msg if msg != null => msg
      }
      .onFinalize(IO {
        subscription.unsubscribe()
      })
  }

}
