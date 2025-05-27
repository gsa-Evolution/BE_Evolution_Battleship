package local.model

// Tracks which ships have been placed by the player.
case class ShipsPlaced(
  submarine: Boolean = false,
  destroyer: Boolean = false,
  cruiser: Boolean = false,
  battleship: Boolean = false,
  carrier: Boolean = false
)

sealed abstract class Ship(coordinates: Coordinates, gameState: PlacementPhase) {
  // Indicates if the maximum number of this ship type has been placed.
  val exceedsMaxNumber = false

  // Checks if the ship's location is valid, not adjacent to other ships.
  protected def validLocation: Boolean = {
    val fullShipCoordinates: Seq[(Int, Int)] =
      for {
        i <- coordinates.start.x to coordinates.end.x
        j <- coordinates.start.y to coordinates.end.y
      } yield (i, j)

    val neighbours: Set[(Int, Int)] = calculateNeighbours(gameState.canvas)

    val invalidLocation: Seq[Boolean] =
      for {
        fullShipCoordinates <- fullShipCoordinates
      } yield neighbours.contains(fullShipCoordinates)

    if (invalidLocation.contains(true)) {
      println("Invalid placement, ships may not be placed in cells adjacent to each other!")
      false
    }
    else true
  }

  // Updates the board for a horizontally placed ship.
  protected def updateHorizontalShip(canvas: Map[(Int, Int), Int]): Map[(Int, Int), Int] = {
    var newCanvas = canvas
    val shipLength = coordinates.end.x - coordinates.start.x + 1

    for (x <- coordinates.start.x to coordinates.end.x) {
      newCanvas = newCanvas + ((x, coordinates.start.y) -> shipLength)
    }

    newCanvas
  }

  // Updates the board for a vertically placed ship.
  protected def updateVerticalShip(canvas: Map[(Int, Int), Int]): Map[(Int, Int), Int] = {
    var newCanvas = canvas
    val shipLength = coordinates.end.y - coordinates.start.y + 1

    for (y <- coordinates.start.y to coordinates.end.y) {
      newCanvas = newCanvas + ((coordinates.start.x, y) -> shipLength)
    }

    newCanvas
  }

  // Calculates all neighbouring cells of existing ships, used for placement validation.
  protected def calculateNeighbours(canvas: Map[(Int, Int), Int]): Set[(Int, Int)] = {
    val shipLocations: Set[(Int, Int)] = canvas.filter(_._2 == 1).keySet

    shipLocations.flatMap(cell =>
      Set(
        (cell._1 - 1, cell._2 - 1),
        (cell._1, cell._2 - 1),
        (cell._1 + 1, cell._2 - 1),
        (cell._1, cell._2 - 1),
        (cell._1, cell._2),
        (cell._1, cell._2 + 1),
        (cell._1 + 1, cell._2 - 1),
        (cell._1 + 1, cell._2),
        (cell._1 + 1, cell._2 + 1)
      )
    )
  }

  // Updates the game state with the ship's new location.
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
      case _ if cruiser    => new Cruiser(coordinates, gameState)
      case _ if destroyer  => new Destroyer(coordinates, gameState)
      case _ if submarine  => new Submarine(coordinates, gameState)
      case _ if battleship => new Battleship(coordinates, gameState)
      case _ if carrier    => new Carrier(coordinates, gameState)
    }
  }

  // Represents a Destroyer ship, length 2.
  class Destroyer(coordinates: Coordinates, gameState: PlacementPhase) extends Ship(coordinates, gameState) {
    private val isHorizontalDestroyer: Boolean =
      coordinates.end.x - coordinates.start.x == 1 && coordinates.end.y - coordinates.start.y == 0

    private val isVerticalDestroyer: Boolean =
      coordinates.end.y - coordinates.start.y == 1 && coordinates.end.x - coordinates.start.x == 0

    override val exceedsMaxNumber: Boolean = gameState.shipsPlaced.destroyer

    // Updates the board and marks the destroyer as placed.
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

  // Represents a Cruiser ship, length 3.
  class Cruiser(coordinates: Coordinates, gameState: PlacementPhase) extends Ship(coordinates, gameState) {
    private val isHorizontalCruiser: Boolean =
      coordinates.end.x - coordinates.start.x == 2 && coordinates.end.y - coordinates.start.y == 0

    private val isVerticalCruiser: Boolean =
      coordinates.end.y - coordinates.start.y == 2 && coordinates.end.x - coordinates.start.x == 0

    override val exceedsMaxNumber: Boolean = gameState.shipsPlaced.cruiser

    // Updates the board and marks the cruiser as placed.
    override def updateLocation(): PlacementPhase = {
      coordinates match {
        case _ if !validLocation | exceedsMaxNumber => gameState
        case _ if isHorizontalCruiser =>
          val newCanvas = updateHorizontalShip(gameState.canvas)
          val cruiserPlaced = gameState.shipsPlaced.copy(cruiser = true)

          PlacementPhase(canvas = newCanvas, shipsPlaced = cruiserPlaced)
        case _ if isVerticalCruiser =>
          val newCanvas = updateVerticalShip(gameState.canvas)
          val cruiserPlaced = gameState.shipsPlaced.copy(cruiser = true)

          gameState.copy(canvas = newCanvas, shipsPlaced = cruiserPlaced)
        case _ => gameState
      }
    }
  }

  // Represents a Submarine ship, length 3.
  class Submarine(coordinates: Coordinates, gameState: PlacementPhase) extends Ship(coordinates, gameState) {
    private val isHorizontalSubmarine: Boolean =
      coordinates.end.x - coordinates.start.x == 2 && coordinates.end.y - coordinates.start.y == 0

    private val isVerticalSubmarine: Boolean =
      coordinates.end.y - coordinates.start.y == 2 && coordinates.end.x - coordinates.start.x == 0

    override val exceedsMaxNumber: Boolean = gameState.shipsPlaced.submarine

    // Updates the board and marks the submarine as placed.
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

  // Represents a Battleship ship, length 4.
  class Battleship(coordinates: Coordinates, gameState: PlacementPhase) extends Ship(coordinates, gameState) {
    private val isHorizontalBattleship: Boolean =
      coordinates.end.x - coordinates.start.x == 3 && coordinates.end.y - coordinates.start.y == 0

    private val isVerticalBattleship: Boolean =
      coordinates.end.y - coordinates.start.y == 3 && coordinates.end.x - coordinates.start.x == 0

    override val exceedsMaxNumber: Boolean = gameState.shipsPlaced.battleship

    // Updates the board and marks the battleship as placed.
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

  // Represents a Carrier ship, length 5.
  class Carrier(coordinates: Coordinates, gameState: PlacementPhase) extends Ship(coordinates, gameState) {
    private val isHorizontalCarrier: Boolean =
      coordinates.end.x - coordinates.start.x == 4 && coordinates.end.y - coordinates.start.y == 0

    private val isVerticalCarrier: Boolean =
      coordinates.end.y - coordinates.start.y == 4 && coordinates.end.x - coordinates.start.x == 0

    override val exceedsMaxNumber: Boolean = gameState.shipsPlaced.carrier

    // Updates the board and marks the carrier as placed.
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
}
