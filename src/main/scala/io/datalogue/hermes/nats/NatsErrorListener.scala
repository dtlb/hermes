package io.datalogue.hermes.nats

import io.nats.client.{Connection, Consumer, ErrorListener}
import org.log4s.getLogger

class NatsErrorListener extends ErrorListener {

  private val log = getLogger
  def errorOccurred(conn: Connection, error: String): Unit = {
    log.info("The server notificed the client with: " + error)
  }

  def exceptionOccurred(conn: Connection, exp: Exception): Unit = {
    log.error("The connection handled an exception: " + exp.getLocalizedMessage)
  }

  def slowConsumerDetected(conn: Connection, consumer: Consumer): Unit = {
    log.warn("A slow consumer was detected.")
  }
}