package io.datalogue.hermes.nats

import io.nats.client.{Connection, Message, Nats, Subscription}

import scala.compat.java8.FutureConverters
import scala.concurrent.Future

class NatsConnection(connection: Connection) {

  def publish(topic: String, body: Array[Byte]) = {
    connection.publish(topic, body)
  }

  def send(topic: String, body: Array[Byte]): Future[Message] = {
    val future = connection.request(topic, body)
    FutureConverters.toScala(future)
  }

  def subscribe(topic: String, f: Message => Unit): Subscription = {
    val dispatcher = connection.createDispatcher(_=>{})
    dispatcher.subscribe(topic, (msg: Message) => {
      f(msg)
    })
  }
}

object NatsConnection {
  def apply(url: String): NatsConnection = {

    new NatsConnection(Nats.connect(url))
  }

  def apply(): NatsConnection = new NatsConnection(Nats.connect())
}
