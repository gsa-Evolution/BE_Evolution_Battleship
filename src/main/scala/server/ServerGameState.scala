package server

import cats.implicits._
import server.Cell.Ship
import server.ServerGameState._
import server.ServerWebSockets.PlayerId

// Sealed trait representing the different states of the game
sealed trait ServerGameState {

  // Returns the set of player IDs in the current game state
  def playerIds: Set[PlayerId]

  // Handles a player joining the game
  def join(playerId: PlayerId): Either[BattleshipError, ServerGameState] =
    this match {
      case AwaitingPlayersServerPhase(players) =>
        val newPlayers = players + playerId
        newPlayers.toList match {
          case firstPlayer :: secondPlayer :: Nil =>
            PlacementServerPhase(
              player1 = PlayerInGame(firstPlayer, Map.empty, List.empty),
              player2 = PlayerInGame(secondPlayer, Map.empty, List.empty)
            ).asRight

          case _ => AwaitingPlayersServerPhase(newPlayers).asRight
        }

      case state: HasPlayers =>
        if (state.playerIds.contains(playerId)) state.asRight
        else BattleshipError.GameAlreadyStarted.asLeft
      case _: WinServerPhase =>
        BattleshipError.GameAlreadyEnded.asLeft
    }

  // Handles placing ships on the board
  def placeShips(
      playerId: PlayerId,
      placements: List[Placement]
  ): Either[BattleshipError, ServerGameState] =
    this match {
      case placementPhase @ PlacementServerPhase(player1, player2) =>
        val playerInGame = placementPhase.player(playerId)

        val actualAmountOfShipsPerType = placements.map { placement =>
          Map(placement.shipType -> 1)
        }.combineAll

        for {
          _     <- Either.cond(
                     playerInGame.board.isEmpty,
                     left = BattleshipError.PlacementAlreadyDone,
                     right = ()
                   )
          _     <- Either.cond(
                     actualAmountOfShipsPerType == ServerGameState.amountOfShipsPerType,
                     left = BattleshipError.WrongAmountOfShips,
                     right = ()
                   )
          board <- placements
                     .foldLeftM(Map.empty[Coordinate, Cell]) { (map, placement) =>
                       val shipCoordinates =
                         placement.startCoordinate.take(placement.orientation, placement.shipType.length)

                       if (shipCoordinates.size != placement.shipType.length) {
                         BattleshipError.ShipOutOfBounds.asLeft
                       } else if (map.keySet.intersect(shipCoordinates).nonEmpty) {
                         BattleshipError.ShipsAreIntersecting.asLeft
                       } else {
                         Right(map ++ shipCoordinates.map(coordinate => coordinate -> Cell.Ship))
                       }
                     }
        } yield {

          val updatedShips: List[Set[Coordinate]] = placements.map { placement =>
            placement.startCoordinate.take(placement.orientation, placement.shipType.length)
          }

          val updatedPlayerInGame: PlayerInGame = playerInGame.copy(
            board = board,
            ships = updatedShips
          )

          val updatedPlacementPhase: PlacementServerPhase = placementPhase.updatePlayer(updatedPlayerInGame)

          if (updatedPlacementPhase.player1.board.nonEmpty && updatedPlacementPhase.player2.board.nonEmpty) {
            // start the game - implement me :)
            val moveNumber: Int = 0

            AttackServerPhase(updatedPlacementPhase.player1, updatedPlacementPhase.player2, moveNumber)
          } else updatedPlacementPhase
        }

      case _: AwaitingPlayersServerPhase => BattleshipError.GameNotStarted.asLeft
      case _: AttackServerPhase          => BattleshipError.GameAlreadyStarted.asLeft
      case _: WinServerPhase             => BattleshipError.GameAlreadyEnded.asLeft
    }

  // Handles attacking ships on the board
  def attackShips(
                   playerId: PlayerId, // Who attacks
                   coordinate: Coordinate // Coordinate on enemy's board where you attack
                 ): Either[BattleshipError, ServerGameState] = {
    this match {
      case attackPhase: AttackServerPhase =>
        // Check if the player is allowed to attack (it's their turn)
        if (attackPhase.movesNow.playerId != playerId) {
          return BattleshipError.NotYourTurn.asLeft
        }

        val opponent = attackPhase.opponentOf(playerId)

        opponent.board.get(coordinate) match {
          case Some(Cell.Ship) =>
            // Hit
            val updatedOpponent = opponent.copy(board = opponent.board.updated(coordinate, Cell.HitShip))

            val opponentShipsLeft = updatedOpponent.board.count { case (_, cell) => cell == Cell.Ship }

            if (opponentShipsLeft == 0) {
              // Game over: attacker wins
              WinServerPhase(attackPhase.movesNow, updatedOpponent).asRight
            } else {
              // Keep the same player's turn on hit (do NOT increment moveNumber)
              val updatedAttackPhase = attackPhase
                .updatePlayer(updatedOpponent)

              updatedAttackPhase.asRight
            }

          case Some(Cell.HitShip | Cell.Miss) =>
            // Already attacked this cell
            BattleshipError.CellAlreadyTaken.asLeft

          case None =>
            // Miss
            val updatedOpponent = opponent.copy(board = opponent.board.updated(coordinate, Cell.Miss))

            // Increment moveNumber to change turn
            val updatedAttackPhase = attackPhase
              .updatePlayer(updatedOpponent)
              .copy(moveNumber = attackPhase.moveNumber + 1)

            updatedAttackPhase.asRight
        }

      case _: AwaitingPlayersServerPhase => BattleshipError.GameNotStarted.asLeft
      case _: PlacementServerPhase       => BattleshipError.GameAlreadyStarted.asLeft
      case _: WinServerPhase             => BattleshipError.GameAlreadyEnded.asLeft
    }
  }
}

object ServerGameState {

  // Defines the amount of ships per type
  val amountOfShipsPerType: Map[ShipType, Int] = Map(ShipType.Cruiser -> 1, ShipType.Destroyer -> 1, ShipType.Submarine -> 1, ShipType.Battleship -> 1, ShipType.Carrier -> 1)

  // Sealed trait representing different errors that can occur in the game
  sealed trait BattleshipError

  object BattleshipError {
    case object GameAlreadyStarted   extends BattleshipError
    case object GameAlreadyEnded     extends BattleshipError
    case object GameNotStarted       extends BattleshipError
    case object PlacementAlreadyDone extends BattleshipError

    case object WrongAmountOfShips   extends BattleshipError
    case object ShipsAreIntersecting extends BattleshipError
    case object ShipOutOfBounds      extends BattleshipError

    case object NotYourTurn      extends BattleshipError
    case object CellAlreadyTaken extends BattleshipError
  }

  // Case class representing the state when awaiting players
  final case class AwaitingPlayersServerPhase(
      players: Set[PlayerId]
  ) extends ServerGameState {
    override def playerIds: Set[PlayerId] = players
  }

  // Sealed trait representing states that have players
  sealed trait HasPlayers extends ServerGameState {
    type Self <: ServerGameState

    def player1: PlayerInGame
    def player2: PlayerInGame

    override final def playerIds: Set[PlayerId] = Set(player1.playerId, player2.playerId)

    final def player(playerId: PlayerId): PlayerInGame =
      if (player1.playerId == playerId) player1
      else player2

    final def opponentOf(playerId: PlayerId): PlayerInGame =
      if (player1.playerId != playerId) player1
      else player2

    def updatePlayer(playerInGame: PlayerInGame): Self
  }

  // Case class representing the state during ship placement
  final case class PlacementServerPhase(
      player1: PlayerInGame,
      player2: PlayerInGame
  ) extends HasPlayers {
    type Self = PlacementServerPhase

    def updatePlayer(playerInGame: PlayerInGame): Self =
      if (player1.playerId == playerInGame.playerId)
        copy(player1 = playerInGame)
      else
        copy(player2 = playerInGame)
  }

  // Case class representing the state during the attack phase
  final case class AttackServerPhase(
      player1: PlayerInGame,
      player2: PlayerInGame,
      moveNumber: Int
  ) extends HasPlayers {
    type Self = AttackServerPhase

    def movesNow: PlayerInGame =
      if (moveNumber % 2 == 0) player1 else player2

    def updatePlayer(playerInGame: PlayerInGame): AttackServerPhase =
      if (player1.playerId == playerInGame.playerId)
        copy(player1 = playerInGame)
      else
        copy(player2 = playerInGame)
  }

  // Case class representing the state when a player wins
  final case class WinServerPhase(
     winner: PlayerInGame,
     loser: PlayerInGame
  ) extends ServerGameState {
    override def playerIds: Set[PlayerId] = Set(winner.playerId, loser.playerId)
  }
}
