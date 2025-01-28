package model.ships

import model.{Coordinates, PlacementPhase, Ship}

class Destroyer(coordinates: Coordinates, gameState: PlacementPhase) extends Ship(coordinates, gameState) {
  private val isHorizontalDestroyer: Boolean =
    coordinates.end.x - coordinates.start.x == 1 && coordinates.end.y - coordinates.start.y == 0

  private val isVerticalDestroyer: Boolean =
    coordinates.end.y - coordinates.start.y == 1 && coordinates.end.x - coordinates.start.x == 0

  protected override val exceedsMaxNumber: Boolean = gameState.shipsPlaced.destroyer

  override def updateLocation(): PlacementPhase = {
    coordinates match {
      case _ if !validLocation | exceedsMaxNumber => gameState
      case _ if isHorizontalDestroyer =>
        val newCanvas = updateHorizontalShip(gameState.canvas)
        val destroyerPlaced = gameState.shipsPlaced.copy(destroyer = true)

        PlacementPhase(canvas = newCanvas, shipsPlaced = destroyerPlaced)
      case _ if isVerticalDestroyer =>
        val newCanvas = updateVerticalShip(gameState.canvas)
        val destroyerPlaced = gameState.shipsPlaced.copy(destroyer = true)

        gameState.copy(canvas = newCanvas, shipsPlaced = destroyerPlaced)
      case _ => gameState
    }
  }
}