package model.ships

import model.{Coordinates, PlacementPhase, Ship}

class Submarine(coordinates: Coordinates, gameState: PlacementPhase) extends Ship(coordinates, gameState) {
  private val isHorizontalSubmarine: Boolean =
    coordinates.end.x - coordinates.start.x == 2 && coordinates.end.y - coordinates.start.y == 0

  private val isVerticalSubmarine: Boolean =
    coordinates.end.y - coordinates.start.y == 2 && coordinates.end.x - coordinates.start.x == 0

  protected override val exceedsMaxNumber: Boolean = gameState.shipsPlaced.submarine

  override def updateLocation(): PlacementPhase = {
    coordinates match {
      case _ if !validLocation | exceedsMaxNumber => gameState
      case _ if isHorizontalSubmarine =>
        val newCanvas = updateHorizontalShip(gameState.canvas)
        val submarinePlaced = gameState.shipsPlaced.copy(submarine = true)

        PlacementPhase(canvas = newCanvas, shipsPlaced = submarinePlaced)
      case _ if isVerticalSubmarine =>
        val newCanvas = updateVerticalShip(gameState.canvas)
        val submarinePlaced = gameState.shipsPlaced.copy(submarine = true)

        gameState.copy(canvas = newCanvas, shipsPlaced = submarinePlaced)
      case _ => gameState
    }
  }
}