package server

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

import java.util.UUID

final case class GameRoomResponse(
                                   id: UUID,
                                   players: Int,
                                   playersNames: List[String],
                                   roomName: String,
                                   sunkShips: Map[String, List[String]],
                                   hasEnded: Boolean
                                 )

object GameRoomResponse {
  // Define the encoder and decoder for GameRoomResponse
  implicit val encoder: Encoder[GameRoomResponse] = deriveEncoder
  implicit val decoder: Decoder[GameRoomResponse] = deriveDecoder
}
