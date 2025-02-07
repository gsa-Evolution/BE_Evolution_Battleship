package server

import io.circe.Decoder
import io.circe.generic.extras.semiauto.deriveEnumerationDecoder

sealed abstract class ShipType(val length: Int)

object ShipType {
  case object Cruiser extends ShipType(1)
  case object Destroyer extends ShipType(2)
  case object Submarine extends ShipType(3)
  case object Battleship extends ShipType(4)
  case object Carrier extends ShipType(5)

  implicit val decoder: Decoder[ShipType] = deriveEnumerationDecoder
}
