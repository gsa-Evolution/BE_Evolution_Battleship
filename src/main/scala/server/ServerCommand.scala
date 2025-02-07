package server

import server.ServerWebSockets.PlayerId

sealed trait ServerCommand

object ServerCommand {
  final case class Join(playerId: PlayerId)                                    extends ServerCommand
  final case class PlaceShips(playerId: PlayerId, placements: List[Placement]) extends ServerCommand
  final case class AttackShips(playerId: PlayerId, coordinate: Coordinate)     extends ServerCommand
}
