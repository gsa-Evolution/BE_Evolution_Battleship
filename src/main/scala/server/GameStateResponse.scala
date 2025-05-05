package server

import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredEncoder
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.deriveEncoder
import server.ServerWebSockets.PlayerId

// Sealed trait representing different game state responses
sealed trait GameStateResponse

object GameStateResponse {

  // Generates a GameStateResponse for a given player based on the current game state
  def forPlayer(playerId: PlayerId, gameState: ServerGameState): GameStateResponse =
    gameState match {
      // Response when awaiting players to join
      case ServerGameState.AwaitingPlayersServerPhase(players) => AwaitingPlayers(players)
      // Response during the ship placement phase
      case placement: ServerGameState.PlacementServerPhase     =>
        val player   = placement.player(playerId)
        val opponent = placement.opponentOf(playerId)
        PlaceShips(
          opponentReady = opponent.board.nonEmpty,
          board = Option.when(player.board.nonEmpty)(player.board)
        )
      // Response during the attack phase
      case attack: ServerGameState.AttackServerPhase           =>
        val player = attack.player(playerId)
        val yourBoard = player.board
        val opponent = attack.opponentOf(playerId)
        val opponentBoard = opponent.board.filter { case (_, cell) => cell != Cell.Ship }
        val youMoveNow: Boolean = attack.movesNow.playerId == playerId
        AttackShips(
          yourTurn = youMoveNow,
          yourBoard = Option.when(yourBoard.nonEmpty)(yourBoard),
          opponentBoard = Option.when(opponentBoard.nonEmpty)(opponentBoard),
        )
      // Response when a player wins the game
      case win: ServerGameState.WinServerPhase                 =>
        val playerWinner = win.winner.playerId
        val playerLoser = win.loser.playerId
        Win(
          winner = playerWinner,
          loser = playerLoser
        )
    }

  // Case class representing the state when awaiting players
  final case class AwaitingPlayers(players: Set[PlayerId]) extends GameStateResponse

  object AwaitingPlayers {
    implicit val encoder: Encoder[AwaitingPlayers] = deriveEncoder
  }

  // Encoder for the board map
  implicit val boardEncoder: Encoder[Map[Coordinate, Cell]] =
    Encoder[List[(Coordinate, Cell)]].contramap(_.toList)

  // Case class representing the state during ship placement
  final case class PlaceShips(
    opponentReady: Boolean,
    // None when you didn't place ships yet
    board: Option[Map[Coordinate, Cell]]
  ) extends GameStateResponse

  object PlaceShips {
    implicit val encoder: Encoder[PlaceShips] = deriveEncoder
  }

  // Case class representing the state during the attack phase
  final case class AttackShips(
    yourTurn: Boolean,
    yourBoard: Option[Map[Coordinate, Cell]],
    opponentBoard: Option[Map[Coordinate, Cell]]
  ) extends GameStateResponse

  object AttackShips {
    implicit val encoder: Encoder[AttackShips] = deriveEncoder
  }

  // Case class representing the state when a player wins
  final case class Win (
    winner: PlayerId,
    loser: PlayerId
  ) extends GameStateResponse

  object Win {
    implicit val encoder: Encoder[Win] = deriveEncoder
  }

  // Encoder for the GameStateResponse trait
  implicit val encoder: Encoder[GameStateResponse] = {
    implicit val configuration: Configuration = Configuration.default.withDiscriminator("type")
    deriveConfiguredEncoder
  }
}
