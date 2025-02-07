package server

import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredEncoder
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.deriveEncoder
import server.ServerWebSockets.PlayerId

sealed trait GameStateResponse

object GameStateResponse {

  def forPlayer(playerId: PlayerId, gameState: ServerGameState): GameStateResponse =
    gameState match {
      case ServerGameState.AwaitingPlayersServerPhase(players) => AwaitingPlayers(players)
      case placement: ServerGameState.PlacementServerPhase     =>
        val player   = placement.player(playerId)
        val opponent = placement.opponentOf(playerId)
        PlaceShips(
          opponentReady = opponent.board.nonEmpty,
          board = Option.when(player.board.nonEmpty)(player.board)
        )

      case attack: ServerGameState.AttackServerPhase           =>
        val player = attack.player(playerId)
        val yourBoard = player.board
        val opponent = attack.opponentOf(playerId)
        val opponentBoard = opponent.board.filter { case (_, cell) => cell != Cell.Ship }
        println("PlayerId" + playerId + "attack.moves" + attack.movesNow.playerId)
        val youMoveNow: Boolean = attack.movesNow.playerId == playerId
        AttackShips(
          yourTurn = youMoveNow,
          yourBoard = Option.when(yourBoard.nonEmpty)(yourBoard),
          opponentBoard = Option.when(opponentBoard.nonEmpty)(opponentBoard),
        )
    }

  final case class AwaitingPlayers(players: Set[PlayerId]) extends GameStateResponse

  object AwaitingPlayers {
    implicit val encoder: Encoder[AwaitingPlayers] = deriveEncoder
  }

  implicit val boardEncoder: Encoder[Map[Coordinate, Cell]] =
    Encoder[List[(Coordinate, Cell)]].contramap(_.toList)

  final case class PlaceShips(
    opponentReady: Boolean,
    // None when you didn't place ships yet
    board: Option[Map[Coordinate, Cell]]
  ) extends GameStateResponse

  object PlaceShips {
    implicit val encoder: Encoder[PlaceShips] = deriveEncoder
  }

  final case class AttackShips(
    yourTurn: Boolean,
    yourBoard: Option[Map[Coordinate, Cell]],
    opponentBoard: Option[Map[Coordinate, Cell]]
  ) extends GameStateResponse

  object AttackShips {
    implicit val encoder: Encoder[AttackShips] = deriveEncoder
  }

  implicit val encoder: Encoder[GameStateResponse] = {
    implicit val configuration: Configuration = Configuration.default.withDiscriminator("type")
    deriveConfiguredEncoder
  }
}
