package model.ships

import model.{Coordinates, PlacementPhase, Ship}

class Cruiser(coordinates: Coordinates, gameState: PlacementPhase) extends Ship(coordinates, gameState) {
  private val isCruiser: Boolean =
    coordinates.start.x == coordinates.end.x && coordinates.start.y == coordinates.end.y

  protected override val exceedsMaxNumber: Boolean = gameState.shipsPlaced.cruiser

  override def updateLocation(): PlacementPhase = {
    coordinates match {
      case _ if !validLocation | exceedsMaxNumber => gameState
      case _ if isCruiser =>
        var newCanvas = gameState.canvas

        newCanvas = newCanvas.transform {
          case (x, _) if x == (coordinates.start.x, coordinates.start.y) => 1
          case (_, v) if v == 1 => 1
          case _ => 0
        }

        val cruiserPlaced = gameState.shipsPlaced.copy(cruiser = true)

        PlacementPhase(canvas = newCanvas, shipsPlaced = cruiserPlaced)
      case _ => gameState
    }
  }
}
