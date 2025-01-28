package model.ships

import model.{Coordinates, PlacementPhase, Ship}

class Battleship(coordinates: Coordinates, gameState: PlacementPhase) extends Ship(coordinates, gameState) {
  private val isHorizontalBattleship: Boolean =
    coordinates.end.x - coordinates.start.x == 3 && coordinates.end.y - coordinates.start.y == 0

  private val isVerticalBattleship: Boolean =
    coordinates.end.y - coordinates.start.y == 3 && coordinates.end.x - coordinates.start.x == 0

  protected override val exceedsMaxNumber: Boolean = gameState.shipsPlaced.battleship

  override def updateLocation(): PlacementPhase = {
    coordinates match {
      case _ if !validLocation | exceedsMaxNumber => gameState
      case _ if isHorizontalBattleship =>
        val newCanvas = updateHorizontalShip(gameState.canvas)
        val battleshipPlaced = gameState.shipsPlaced.copy(battleship = true)

        PlacementPhase(canvas = newCanvas, shipsPlaced = battleshipPlaced)
      case _ if isVerticalBattleship =>
        val newCanvas = updateVerticalShip(gameState.canvas)
        val battleshipPlaced = gameState.shipsPlaced.copy(battleship = true)

        PlacementPhase(canvas = newCanvas, shipsPlaced = battleshipPlaced)
      case _ => gameState
    }
  }
}
