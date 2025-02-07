package server

import io.circe.Decoder
import io.circe.generic.extras.semiauto.deriveEnumerationDecoder

sealed trait Orientation

object Orientation {
  case object Vertical   extends Orientation
  case object Horizontal extends Orientation

  implicit val decoder: Decoder[Orientation] = deriveEnumerationDecoder
}
