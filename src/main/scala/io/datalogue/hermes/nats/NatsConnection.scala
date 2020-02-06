package io.datalogue.hermes.nats


import java.util.concurrent.{LinkedBlockingQueue, TimeUnit}

import cats.effect.IO
import io.nats.streaming.{Options, StreamingConnection, Subscription, SubscriptionOptions, Message => StreamMessage}
import org.log4s.getLogger


class NatsConnection(connection: StreamingConnection) {

  private val log = getLogger

  def publish(topic: String, body: Array[Byte]) = {
    connection.publish(topic, body)
  }

  def subscribeAsStream(topic: String, options: Option[SubscriptionOptions] = None): fs2.Stream[IO, StreamMessage] = {
    val q = new LinkedBlockingQueue[StreamMessage]

    subscribe(topic, (msg: StreamMessage) => {
      q.put(msg)
      log.info(s"received message, queue size ${q.size}, ${msg.getSequence}")
    }, options)

    fs2.Stream.iterate(1)(_ + 1)
        .map(_ => {
          val msg = q.poll(1, TimeUnit.SECONDS)
          if (msg != null) {
            log.info(s"pull message, queue size ${q.size}")
          } else {
            log.debug("no messages in queue")
          }
          msg
        })
      .collect {
        case msg if msg != null => msg
      }
  }

  def subscribe(topic: String, f: StreamMessage => Unit, options: Option[SubscriptionOptions] = None): Subscription = {
    connection.subscribe(topic, (msg: StreamMessage) => {
      f(msg)
    }, options match {
      case None => new SubscriptionOptions.Builder()
        .deliverAllAvailable()
        .build()
      case Some(options) => options
    })
  }

  def close(): Unit = {
    connection.close()
  }
}

object NatsConnection {
  def apply(url: String, clusterId: String, clientId: String): NatsConnection = {

    import io.nats.streaming.StreamingConnectionFactory
    val opts: Options = new Options.Builder()
      .clientId(clientId)
      .clusterId(clusterId)
      .natsUrl(url)
      .build()
    val con = new StreamingConnectionFactory(opts).createConnection()
    new NatsConnection(con)
  }

}
