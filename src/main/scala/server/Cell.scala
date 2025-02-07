package server

import io.circe.{Codec, Decoder}
import io.circe.generic.extras.semiauto.{deriveEnumerationDecoder, deriveEnumerationCodec}

sealed trait Cell

object Cell {
  case object Ship    extends Cell
  case object HitShip extends Cell
  case object Miss    extends Cell

  implicit val codec: Codec[Cell] = deriveEnumerationCodec
}
