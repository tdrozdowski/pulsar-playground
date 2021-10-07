package dev.xymox.pulsar.playground

import com.sksamuel.pulsar4s.{Consumer, ConsumerConfig, Producer, ProducerConfig, PulsarAsyncClient, PulsarClient, Subscription, Topic}
import org.apache.pulsar.client.api.{MessageRoutingMode, Schema, SubscriptionType}
import zio._
import zio.clock.Clock
import zio.console._
import zio.duration._

object MultipleConsumersExample extends App {

  import com.sksamuel.pulsar4s.zio.ZioAsyncHandler._

  implicit val schema: Schema[String] = Schema.STRING

  val client: PulsarAsyncClient       = PulsarClient("pulsar://192.168.1.74:6650")
  val topic: Topic                    = Topic("persistent://playground/examples/my-topic-3")
  val subscription: Subscription      = Subscription("my-subscription-3")
  val otherSubscription: Subscription = Subscription("my-subscription-3-1")

  val primaryConsumer: Consumer[String] =
    client.consumer(
      ConsumerConfig(
        topics = Seq(topic),
        subscriptionName = subscription,
        consumerName = Option("First Consumer"),
        subscriptionType = Option(SubscriptionType.Shared)
      )
    )

  val secondaryConsumer: Consumer[String] =
    client.consumer(
      ConsumerConfig(
        topics = Seq(topic),
        subscriptionName = subscription,
        consumerName = Option("Second Consumer"),
        subscriptionType = Option(SubscriptionType.Shared)
      )
    )

  val producer: Producer[String] = client.producer(ProducerConfig(topic = topic, messageRoutingMode = Option(MessageRoutingMode.RoundRobinPartition)))

  val producerProgram: ZIO[Console with Clock, Throwable, Unit] = for {
    instant <- ZIO.serviceWith[Clock.Service](_.instant)
    _       <- putStrLn(s"Producing message at: $instant")
    _       <- producer.sendAsync(s"Hello at ${instant}!")
  } yield ()

  def consumerProgram(consumer: Consumer[String], consumerName: String): ZIO[Console, Throwable, Unit] = for {
    message <- consumer.receiveAsync
    _       <- putStrLn(s"==> [${consumerName}] Consumed message: ${message.messageId} - Message: ${message.value}")
    _       <- consumer.acknowledgeAsync(message.messageId)
  } yield ()

  val forever: Schedule[Any, Any, Long]                  = Schedule.forever
  val producerSchedule: Schedule[Any, Any, (Long, Long)] = forever && Schedule.spaced(10.milliseconds)

  val program: ZIO[Console with Clock, Throwable, Unit] = for {
    _  <- putStrLn("Starting up...")
    _  <- putStrLn("Control-C to stop...")
    c1 <- consumerProgram(primaryConsumer, "primary-consumer").repeat(forever).fork
    c2 <- consumerProgram(secondaryConsumer, "secondary-consumer").repeat(forever).fork
    p  <- producerProgram.repeat(producerSchedule).fork
    _  <- putStrLn("[Press Any Key to Stop]") *> getStrLn *> c1.interrupt *> c2.interrupt *> p.interrupt *> ZIO.effect(primaryConsumer.close()) *> ZIO
      .effect(
        secondaryConsumer.close()
      ) *> ZIO.effect(
      producer.close()
    ) *> putStrLn("Stopped.")
  } yield ()

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = program.provideLayer(ZEnv.live).exitCode
}
