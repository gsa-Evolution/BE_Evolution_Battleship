package model

import model.ships.{Battleship, Cruiser, Destroyer, Submarine, Carrier}

case class ShipsPlaced(
                         submarine: Boolean = false,
                         destroyer: Boolean = false,
                         cruiser: Boolean = false,
                         battleship: Boolean = false,
                         carrier: Boolean = false
                     )

class Ship(coordinates: Coordinates, gameState: PlacementPhase) {
  protected val exceedsMaxNumber = false

  protected def validLocation: Boolean = {
    val fullShipCoordinates: Seq[(Int, Int)] =
      for {
        i <- coordinates.start.x to coordinates.end.x
        j <- coordinates.start.y to coordinates.end.y
      } yield (i, j)

    val neighbours: Set[(Int, Int)] = Game.calculateNeighbours(gameState.canvas)

    val invalidLocation: Seq[Boolean] =
      for {
        fullShipCoordinates <- fullShipCoordinates
      } yield neighbours.contains(fullShipCoordinates)

    if (invalidLocation.contains(true)) false
    else true
  }

  protected def updateHorizontalShip(canvas: Map[(Int, Int), Int]): Map[(Int, Int), Int] = {
    var newCanvas = canvas
    val shipLength = coordinates.end.x - coordinates.start.x + 1  // calculate the horizontal ship length

    // Iterate through each position from start.x to end.x and update the canvas with the ship length
    for (x <- coordinates.start.x to coordinates.end.x) {
      newCanvas = newCanvas + ((x, coordinates.start.y) -> shipLength) // Assign the ship length to each position
    }

    newCanvas
  }

  protected def updateVerticalShip(canvas: Map[(Int, Int), Int]): Map[(Int, Int), Int] = {
    var newCanvas = canvas
    val shipLength = coordinates.end.y - coordinates.start.y + 1  // calculate the vertical ship length

    //var finalLength = shipLength

    /*if (shipLength == 2) finalLength = 6
    if (shipLength == 3) finalLength = 7*/

    // Iterate through each position from start.y to end.y and update the canvas with the ship length
    for (y <- coordinates.start.y to coordinates.end.y) {
      newCanvas = newCanvas + ((coordinates.start.x, y) -> shipLength) // Assign the ship length to each position
    }

    newCanvas
  }

  def updateLocation(): PlacementPhase = gameState
}

object Ship {
  def apply(coordinates: Coordinates, gameState: PlacementPhase): Ship = {
    val cruiser = coordinates.start.x == coordinates.end.x && coordinates.start.y == coordinates.end.y

    val destroyer =
      coordinates.end.x - coordinates.start.x == 1 && coordinates.end.y - coordinates.start.y == 0 |
        coordinates.end.y - coordinates.start.y == 1 && coordinates.end.x - coordinates.start.x == 0

    val submarine =
      coordinates.end.x - coordinates.start.x == 2 && coordinates.end.y - coordinates.start.y == 0 |
        coordinates.end.y - coordinates.start.y == 2 && coordinates.end.x - coordinates.start.x == 0

    val battleship =
      coordinates.end.x - coordinates.start.x == 3 && coordinates.end.y - coordinates.start.y == 0 |
        coordinates.end.y - coordinates.start.y == 3 && coordinates.end.x - coordinates.start.x == 0

    val carrier =
      coordinates.end.x - coordinates.start.x == 4 && coordinates.end.y - coordinates.start.y == 0 |
        coordinates.end.y - coordinates.start.y == 4 && coordinates.end.x - coordinates.start.x == 0

    coordinates match {
      case _ if cruiser => new Cruiser(coordinates, gameState)
      case _ if destroyer => new Destroyer(coordinates, gameState)
      case _ if submarine => new Submarine(coordinates, gameState)
      case _ if battleship => new Battleship(coordinates, gameState)
      case _ if carrier => new Carrier(coordinates, gameState)
      case _ => new Ship(coordinates, gameState)
    }
  }
}
