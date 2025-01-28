package model.ships

import model.{Coordinates, PlacementPhase, Ship}

class Carrier(coordinates: Coordinates, gameState: PlacementPhase) extends Ship(coordinates, gameState) {
  private val isHorizontalCarrier: Boolean =
    coordinates.end.x - coordinates.start.x == 4 && coordinates.end.y - coordinates.start.y == 0

  private val isVerticalCarrier: Boolean =
    coordinates.end.y - coordinates.start.y == 4 && coordinates.end.x - coordinates.start.x == 0

  protected override val exceedsMaxNumber: Boolean = gameState.shipsPlaced.carrier

  override def updateLocation(): PlacementPhase = {
    coordinates match {
      case _ if !validLocation | exceedsMaxNumber => gameState
      case _ if isHorizontalCarrier =>
        val newCanvas = updateHorizontalShip(gameState.canvas)
        val carrierPlaced = gameState.shipsPlaced.copy(carrier = true)

        PlacementPhase(canvas = newCanvas, shipsPlaced = carrierPlaced)
      case _ if isVerticalCarrier =>
        val newCanvas = updateVerticalShip(gameState.canvas)
        val carrierPlaced = gameState.shipsPlaced.copy(carrier = true)

        gameState.copy(canvas = newCanvas, shipsPlaced = carrierPlaced)
      case _ => gameState
    }
  }
}