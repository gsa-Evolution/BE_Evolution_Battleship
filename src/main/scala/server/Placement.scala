package server

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

final case class Placement(
    startCoordinate: Coordinate,
    shipType: ShipType,
    orientation: Orientation
)

object Placement {
  implicit val decoder: Decoder[Placement] = deriveDecoder
}
