package dev.xymox.pulsar.playground.event

import zio.json.ast.Json
import zio.json.{JsonCodec, JsonDecoder, JsonEncoder}

import java.time.Instant

sealed trait CartEvent

object CartEvent {
  case object AddedToCart     extends CartEvent
  case object QuantityUpdated extends CartEvent
  case object RemovedFromCart extends CartEvent

  private def fromName(name: String): CartEvent = name match {
    case "AddedToCart"     => AddedToCart
    case "QuantityUpdated" => QuantityUpdated
    case "RemovedFromCart" => RemovedFromCart
  }

  implicit val encoder: JsonEncoder[CartEvent] = JsonEncoder[String].contramap(_.toString)
  implicit val decoder: JsonDecoder[CartEvent] = JsonDecoder[String].map(fromName)
  implicit val codec: JsonCodec[CartEvent]     = JsonCodec[CartEvent](encoder, decoder)
}

case class ShoppingCartEvent(eventType: CartEvent, when: Instant, cartSessionId: String, entity: Json)

case class Item(id: String, name: String, description: String, price: Double)

case class Cart(items: Seq[Item], userEmail: String, sessionId: String)
