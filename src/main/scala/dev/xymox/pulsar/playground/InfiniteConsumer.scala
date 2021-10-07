package dev.xymox.pulsar.playground

import zio._
import zio.clock._
import zio.console._
import zio.duration._
import com.sksamuel.pulsar4s._
import org.apache.pulsar.client.api.Schema

object InfiniteConsumer extends App {

  import com.sksamuel.pulsar4s.zio.ZioAsyncHandler._

  implicit val schema: Schema[String] = Schema.STRING

  val client: PulsarAsyncClient = PulsarClient("pulsar://192.168.1.74:6650")
  val topic: Topic              = Topic("persistent://playground/examples/my-topic-2")

  val consumer: Consumer[String] = client.consumer(ConsumerConfig(topics = Seq(topic), subscriptionName = Subscription("my-subscription-2")))
  val producer: Producer[String] = client.producer(ProducerConfig(topic = topic))

  val producerProgram: ZIO[Console with Clock, Throwable, Unit] = for {
    instant <- ZIO.serviceWith[Clock.Service](_.instant)
    _       <- putStrLn(s"Producing message at: $instant")
    _       <- producer.sendAsync(s"Hello at ${instant}!")
  } yield ()

  val consumerProgram: ZIO[Console, Throwable, Unit] = for {
    _       <- ZIO.effect(consumer.seekLatest()) // needed?
    message <- consumer.receiveAsync
    _       <- putStrLn(s"==> Consumed message: ${message.messageId} - Message: ${message.value}")
    _       <- consumer.acknowledgeAsync(message.messageId)
  } yield ()

  val forever: Schedule[Any, Any, Long] = Schedule.forever
  val producerSchedule                  = forever && Schedule.spaced(1.seconds)

  val program: ZIO[Console with Clock, Throwable, Unit] = for {
    _ <- putStrLn("Starting up...")
    _ <- putStrLn("Control-C to stop...")
    c <- consumerProgram.repeat(forever).fork
    p <- producerProgram.repeat(producerSchedule).fork
    _ <- putStrLn("[Press Any Key to Stop]") *> getStrLn *> c.interrupt *> p.interrupt *> ZIO.effect(consumer.close()) *> ZIO.effect(
      producer.close()
    ) *> putStrLn("Stopped.")
  } yield ()

  override def run(args: List[String]): URIO[ZEnv, ExitCode] = program.provideLayer(ZEnv.live).exitCode
}
