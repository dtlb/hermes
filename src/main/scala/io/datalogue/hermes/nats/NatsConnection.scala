package io.datalogue.hermes.nats


import io.nats.streaming.{Options, StreamingConnection, Subscription, SubscriptionOptions, Message => StreamMessage}


class NatsConnection(connection: StreamingConnection) {

  def publish(topic: String, body: Array[Byte]) = {
    connection.publish(topic, body)
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
