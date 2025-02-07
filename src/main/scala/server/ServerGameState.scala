package server

import cats.implicits._
import server.Cell.Ship
import server.ServerGameState._
import server.ServerWebSockets.PlayerId

sealed trait ServerGameState {

  def playerIds: Set[PlayerId]

  def join(playerId: PlayerId): Either[BattleshipError, ServerGameState] =
    this match {
      case AwaitingPlayersServerPhase(players) =>
        val newPlayers = players + playerId
        newPlayers.toList match {
          case firstPlayer :: secondPlayer :: Nil =>
            PlacementServerPhase(
              player1 = PlayerInGame(firstPlayer, Map.empty),
              player2 = PlayerInGame(secondPlayer, Map.empty)
            ).asRight

          case _ => AwaitingPlayersServerPhase(newPlayers).asRight
        }
      case _                                   => BattleshipError.GameAlreadyStarted.asLeft
    }

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

          val updatedPlayerInGame: PlayerInGame = playerInGame.copy(board = board)
          val updatedPlacementPhase: PlacementServerPhase = placementPhase.updatePlayer(updatedPlayerInGame)

          if (updatedPlacementPhase.player1.board.nonEmpty && updatedPlacementPhase.player2.board.nonEmpty) {
            // start the game - implement me :)
            val moveNumber: Int = 0
            AttackServerPhase(player1, player2, moveNumber)
          } else updatedPlacementPhase
        }

      case _: AwaitingPlayersServerPhase => BattleshipError.GameNotStarted.asLeft
      case _: AttackServerPhase          => BattleshipError.GameAlreadyStarted.asLeft
//      case _: WinServerPhase           => BattleshipError.GameAlreadyEnded.asLeft
    }

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
          case Some(Ship) =>
            val updatedOpponent = opponent.copy(board = opponent.board.updated(coordinate, Cell.HitShip))

            val opponentShipsLeft = updatedOpponent.board.count { case (_, cell) => cell == Cell.Ship }

            if (opponentShipsLeft == 0) {
              // Goes to the win phase state and when a player wants to send a new message it displays an error saying there is already an winner
            }

            val updatedAttackPhase = attackPhase.copy(
              moveNumber = attackPhase.moveNumber + 1,
            )

            updatedAttackPhase.updatePlayer(updatedOpponent).asRight

          case Some(Cell.HitShip | Cell.Miss) =>
            BattleshipError.CellAlreadyTaken.asLeft

          case None =>
            val updatedOpponent: PlayerInGame = opponent.copy(board = opponent.board.updated(coordinate, Cell.Miss))

            val updatedAttackPhase = attackPhase.copy(
              moveNumber = attackPhase.moveNumber + 1,
            )

            updatedAttackPhase.updatePlayer(updatedOpponent).asRight
        }

      case _ => BattleshipError.GameNotStarted.asLeft
    }
  }
}

object ServerGameState {

  val amountOfShipsPerType: Map[ShipType, Int] = Map(ShipType.Cruiser -> 1, ShipType.Destroyer -> 1, ShipType.Submarine -> 1, ShipType.Battleship -> 1, ShipType.Carrier -> 1)

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

  final case class AwaitingPlayersServerPhase(
      players: Set[PlayerId]
  ) extends ServerGameState {
    override def playerIds: Set[PlayerId] = players
  }

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
}
