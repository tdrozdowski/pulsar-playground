package dev.xymox.pulsar.playground

import org.apache.pulsar.client.api.Schema
import zio._
import zio.console._
import com.sksamuel.pulsar4s._

object Main extends App {

  import com.sksamuel.pulsar4s.zio.ZioAsyncHandler._

  implicit val schema: Schema[String] = Schema.STRING

  val client: PulsarAsyncClient = PulsarClient("pulsar://192.168.1.74:6650")
  val topic: Topic              = Topic("persistent://playground/examples/my-topic")

  val consumer: Consumer[String] = client.consumer(ConsumerConfig(topics = Seq(topic), subscriptionName = Subscription("my-subscription")))
  val producer: Producer[String] = client.producer(ProducerConfig(topic = topic))

  val app: ZIO[Console, Throwable, Unit] =
    for {
      _  <- putStrLn("Getting ready to start...")
      _  <- ZIO.effect(consumer.seekLatest())
      m1 <- producer.sendAsync("Hello!")
      _  <- putStrLn(s"Sent message: ${m1}")
      m  <- consumer.receiveAsync
      _  <- putStrLn(s"Received: (id: ${m.messageId} - ${m.value}")
      _  <- consumer.acknowledgeAsync(m.messageId)
      _  <- putStrLn("Completed.")
    } yield ()

  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    app.provideLayer(Console.live).exitCode
}
