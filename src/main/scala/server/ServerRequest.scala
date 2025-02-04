package server

import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import io.circe.generic.semiauto.deriveDecoder
import local.model.Coordinates

sealed trait ServerRequest

object ServerRequest {
  final case class MakeMove(coordinate: Coordinates) extends ServerRequest
  object MakeMove {
    implicit val decoder: Decoder[MakeMove] = deriveDecoder
  }

  implicit val decoder: Decoder[ServerRequest] = {
    implicit val configuration: Configuration = Configuration.default.withDiscriminator("type")
    deriveConfiguredDecoder
  }
}