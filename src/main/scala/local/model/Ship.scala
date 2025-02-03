package local.model

//import local.model.ships.{Battleship, Carrier, Cruiser, Destroyer, Submarine}

case class ShipsPlaced(
                         submarine: Boolean = false,
                         destroyer: Boolean = false,
                         cruiser: Boolean = false,
                         battleship: Boolean = false,
                         carrier: Boolean = false
                     )

sealed abstract class Ship(coordinates: Coordinates, gameState: PlacementPhase) {
  val exceedsMaxNumber = false

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

    if (invalidLocation.contains(true)) false
    else true
  }

  protected def updateHorizontalShip(canvas: Map[(Int, Int), Int]): Map[(Int, Int), Int] = {
    var newCanvas = canvas
    val shipLength = coordinates.end.x - coordinates.start.x + 1

    for (x <- coordinates.start.x to coordinates.end.x) {
      newCanvas = newCanvas + ((x, coordinates.start.y) -> shipLength)
    }

    newCanvas
  }

  protected def updateVerticalShip(canvas: Map[(Int, Int), Int]): Map[(Int, Int), Int] = {
    var newCanvas = canvas
    val shipLength = coordinates.end.y - coordinates.start.y + 1

    for (y <- coordinates.start.y to coordinates.end.y) {
      newCanvas = newCanvas + ((coordinates.start.x, y) -> shipLength)
    }

    newCanvas
  }

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
      //case _ => new Ship(coordinates, gameState)
    }
  }

  class Cruiser(coordinates: Coordinates, gameState: PlacementPhase) extends Ship(coordinates, gameState) {
    private val isCruiser: Boolean =
      coordinates.start.x == coordinates.end.x && coordinates.start.y == coordinates.end.y

    override val exceedsMaxNumber: Boolean = gameState.shipsPlaced.cruiser

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

  class Destroyer(coordinates: Coordinates, gameState: PlacementPhase) extends Ship(coordinates, gameState) {
    private val isHorizontalDestroyer: Boolean =
      coordinates.end.x - coordinates.start.x == 1 && coordinates.end.y - coordinates.start.y == 0

    private val isVerticalDestroyer: Boolean =
      coordinates.end.y - coordinates.start.y == 1 && coordinates.end.x - coordinates.start.x == 0

    override val exceedsMaxNumber: Boolean = gameState.shipsPlaced.destroyer

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

  class Submarine(coordinates: Coordinates, gameState: PlacementPhase) extends Ship(coordinates, gameState) {
    private val isHorizontalSubmarine: Boolean =
      coordinates.end.x - coordinates.start.x == 2 && coordinates.end.y - coordinates.start.y == 0

    private val isVerticalSubmarine: Boolean =
      coordinates.end.y - coordinates.start.y == 2 && coordinates.end.x - coordinates.start.x == 0

    override val exceedsMaxNumber: Boolean = gameState.shipsPlaced.submarine

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

  class Battleship(coordinates: Coordinates, gameState: PlacementPhase) extends Ship(coordinates, gameState) {
    private val isHorizontalBattleship: Boolean =
      coordinates.end.x - coordinates.start.x == 3 && coordinates.end.y - coordinates.start.y == 0

    private val isVerticalBattleship: Boolean =
      coordinates.end.y - coordinates.start.y == 3 && coordinates.end.x - coordinates.start.x == 0

    override val exceedsMaxNumber: Boolean = gameState.shipsPlaced.battleship

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

  class Carrier(coordinates: Coordinates, gameState: PlacementPhase) extends Ship(coordinates, gameState) {
    private val isHorizontalCarrier: Boolean =
      coordinates.end.x - coordinates.start.x == 4 && coordinates.end.y - coordinates.start.y == 0

    private val isVerticalCarrier: Boolean =
      coordinates.end.y - coordinates.start.y == 4 && coordinates.end.x - coordinates.start.x == 0

    override val exceedsMaxNumber: Boolean = gameState.shipsPlaced.carrier

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
