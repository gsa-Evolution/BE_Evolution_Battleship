package server

import local.model.Coordinates
import server.ServerWebSockets.PlayerId

sealed trait ServerCommand

object ServerCommand {
  final case class Join(playerId: PlayerId)                              extends ServerCommand
  final case class MakeMove(playerId: PlayerId, coordinate: Coordinates) extends ServerCommand
}