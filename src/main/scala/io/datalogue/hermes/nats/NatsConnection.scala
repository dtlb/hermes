package io.datalogue.hermes.nats


import java.util.concurrent.{LinkedBlockingQueue, TimeUnit}

import cats.effect.IO
import io.nats.streaming.{Options, StreamingConnection, Subscription, SubscriptionOptions, Message => StreamMessage, StreamingConnectionFactory}
import org.log4s.getLogger

import scala.util.Try


class NatsConnection(connection: StreamingConnection) {

  private val log = getLogger

  def publish(topic: String, body: Array[Byte]): IO[Unit] =
    IO.fromTry(Try(connection.publish(topic, body)))

  def subscribeAsStream(topic: String, options: Option[SubscriptionOptions] = None): fs2.Stream[IO, StreamMessage] = {
    val q = new LinkedBlockingQueue[StreamMessage]
    for {
      _ <- fs2.Stream.eval(subscribe(topic, (msg: StreamMessage) => {
        q.put(msg)
        log.info(s"received message, queue size ${q.size}, ${msg.getSequence}")
      }, options))
      s <- fs2.Stream.iterate(1)(_ + 1)
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
    } yield s
  }

  def subscribe(topic: String, f: StreamMessage => Unit, options: Option[SubscriptionOptions] = None): IO[Subscription] = {
    IO.fromTry(Try(connection.subscribe(topic, (msg: StreamMessage) => {
      f(msg)
    }, options match {
      case None => new SubscriptionOptions.Builder()
        .deliverAllAvailable()
        .build()
      case Some(options) => options
    })))
  }

  def close(): IO[Unit] = {
    IO.fromTry(Try(connection.close()))
  }
}

object NatsConnection {

  def connect(url: String, clusterId: String, clientId: String): IO[NatsConnection] = {

    val opts: Options = new Options.Builder()
      .clientId(clientId)
      .clusterId(clusterId)
      .errorListener(new NatsErrorListener())
      .natsUrl(url)
      .build()

    IO.fromTry(Try(new StreamingConnectionFactory(opts).createConnection()))
      .map(new NatsConnection(_))
  }



}




