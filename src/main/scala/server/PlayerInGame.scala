package server

import server.ServerWebSockets.PlayerId

final case class PlayerInGame(
    playerId: PlayerId,
    board: Map[Coordinate, Cell]
)
