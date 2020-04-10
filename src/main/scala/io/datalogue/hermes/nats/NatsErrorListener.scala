package io.datalogue.hermes.nats

import io.nats.client.{Connection, Consumer, ErrorListener}

class NatsErrorListener extends ErrorListener {
  def errorOccurred(conn: Connection, error: String): Unit = {
    System.out.println("The server notificed the client with: " + error)
  }

  def exceptionOccurred(conn: Connection, exp: Exception): Unit = {
    System.out.println("The connection handled an exception: " + exp.getLocalizedMessage)
  }

  def slowConsumerDetected(conn: Connection, consumer: Consumer): Unit = {
    System.out.println("A slow consumer was detected.")
  }
}