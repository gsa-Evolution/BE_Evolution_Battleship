package server

import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import io.circe.generic.semiauto.deriveDecoder
import local.model.Coordinates

sealed trait ServerRequest

object ServerRequest {
  final case class PlaceShips(placements: List[Placement]) extends ServerRequest
  object PlaceShips {
    implicit val decoder: Decoder[PlaceShips] = deriveDecoder
  }

  final case class AttackShips(coordinate: Coordinate) extends ServerRequest
  object AttackShips {
    implicit val decoder : Decoder[AttackShips] = deriveDecoder
  }

  implicit val decoder: Decoder[ServerRequest] = {
    implicit val configuration: Configuration = Configuration.default.withDiscriminator("type")
    deriveConfiguredDecoder
  }
}
