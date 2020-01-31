package io.datalogue.hermes.nats

import io.nats.streaming.{Options, StreamingConnectionFactory}
import org.scalatest.{Matchers, WordSpec}

import scala.io.Source

class NatsStreamSubscriberSpec extends WordSpec with Matchers {

  private val host = "localhost:4222"

  val opts: Options = new Options.Builder()
    .clientId("consumer")
    .clusterId("test-cluster")
    .natsUrl(host)
    .build()
  val con = new StreamingConnectionFactory(opts).createConnection()

  "NatsSubscriber" should {
    "subscribe to topic" in {

      val oldCon = con.getNatsConnection
      oldCon.publish("topic", "First".getBytes())

      val subscriber = new NatsStreamSubscriber(con)
      val stream = subscriber.subscribe("topic")

      oldCon.publish("topic", "Second".getBytes())
      oldCon.publish("topic", "Third".getBytes())

      val result = stream.take(2).compile.toList.unsafeRunSync()

      result.collect {
        case msg if msg != null => new String(msg.getData)
      } should be (List("Second", "Third"))


    }

  }

  def get(url: String,
          connectTimeout: Int = 5000,
          readTimeout: Int = 5000,
          requestMethod: String = "GET") =
  {
    import java.net.{URL, HttpURLConnection}
    val connection = (new URL(url)).openConnection.asInstanceOf[HttpURLConnection]
    connection.setConnectTimeout(connectTimeout)
    connection.setReadTimeout(readTimeout)
    connection.setRequestMethod(requestMethod)
    val inputStream = connection.getInputStream
    val content = Source.fromInputStream(inputStream).mkString
    if (inputStream != null) inputStream.close
    content
  }

}

