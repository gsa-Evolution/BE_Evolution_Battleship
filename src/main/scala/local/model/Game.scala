package local.model

import Board._
import Coordinates._

// Represents which player is currently active.
sealed trait ActivePlayer
case object PlayerOne extends ActivePlayer
case object PlayerTwo extends ActivePlayer

// Represents the state of the game during the attack phase.
case class AttackPhase(
  playerOneCanvas: Map[(Int, Int), Int],     // Player One's board.
  playerTwoCanvas: Map[(Int, Int), Int],     // Player Two's board.
  activePlayer: ActivePlayer,                // Whose turn it is.
  playerOneHistory: Set[(Int, Int)] = Set(), // Coordinates Player One has attacked.
  playerTwoHistory: Set[(Int, Int)] = Set()  // Coordinates Player Two has attacked.
)

// Represents the state of the game during the ship placement phase.
case class PlacementPhase(
  canvas: Map[(Int, Int), Int] = Map(),    // The board for placing ships.
  shipsPlaced: ShipsPlaced = ShipsPlaced() // Tracks which ships have been placed.
) {
  // Checks if all ships have been placed.
  def allShipsPlaced: Boolean = shipsPlaced.carrier && shipsPlaced.cruiser && shipsPlaced.destroyer && shipsPlaced.submarine && shipsPlaced.battleship

  // Places a ship on the board using start and end coordinates.
  def placeShip(
    startX: String,
    startY: String,
    endX: String,
    endY: String
  ): PlacementPhase = {
    val ship = Ship(
      Coordinates(
        Start(
          x = calculateCoordinates(startX),
          y = calculateCoordinates(startY)
        ),
        End(
          x = calculateCoordinates(endX),
          y = calculateCoordinates(endY)
        )
      ),
      this
    )

    // Updates the board with the new ship's location.
    ship.updateLocation()
  }
}

object Game {
  // Creates a new empty board for the placement phase.
  def createNewBoard: PlacementPhase = PlacementPhase(canvas = newCanvas)

  // Handles an attack on the board and updates the game state accordingly.
  def attack(coordinateX: String, coordinateY: String, gameState: AttackPhase): AttackPhase = {
    val x: Int = calculateCoordinates(coordinateX)
    val y: Int = calculateCoordinates(coordinateY)

    // Checks if the move has already been made.
    def isRepeatedMove(history: Set[(Int, Int)]): Boolean = {
      history.contains((x, y))
    }

    // Registers the move in the player's history.
    def registerMove(history: Set[(Int, Int)]): Set[(Int, Int)] = {
      history + ((x, y))
    }

    // Updates the board based on the attack result.
    val updateCanvas = (canvas: Map[(Int, Int), Int]) =>
      canvas.transform {
        case (k, v) if k == (x, y) && v == 1 => println("\nWOW! It's a hit! \uD83D\uDCA5"); println("The boat hit was the Cruiser! (1 hole)\n"); 6
        case (k, v) if k == (x, y) && v == 2 => println("\nWOW! It's a hit! \uD83D\uDCA5"); println("The boat hit was the Destroyer! (2 holes)\n"); 6
        case (k, v) if k == (x, y) && v == 3 => println("\nWOW! It's a hit! \uD83D\uDCA5"); println("The boat hit was the Submarine! (3 holes)\n"); 6
        case (k, v) if k == (x, y) && v == 4 => println("\nWOW! It's a hit! \uD83D\uDCA5"); println("The boat hit was the Battleship! (4 holes)\n"); 6
        case (k, v) if k == (x, y) && v == 5 => println("\nWOW! It's a hit! \uD83D\uDCA5"); println("The boat hit was the Carrier! (5 holes)\n"); 6
        case (k, v) if k == (x, y) && v == 0 => println("\nUPS! It's a miss! \uD83D\uDCA6\n"); 7
        case (_, v) => v
      }

    gameState match {
      case AttackPhase(playerOneCanvas, playerTwoCanvas, activePlayer, playerOneHistory, playerTwoHistory) =>
        if (activePlayer == PlayerOne) {
          if (isRepeatedMove(playerOneHistory)) {
            println(s"\nRepeated coordinates! Please play again, Player 1.\n")
            gameState
          } else {
            AttackPhase(
              playerOneCanvas = playerOneCanvas,
              playerTwoCanvas = updateCanvas(playerTwoCanvas),
              activePlayer = PlayerTwo,
              playerOneHistory = registerMove(playerOneHistory),
              playerTwoHistory = playerTwoHistory
            )
          }
        } else if (activePlayer == PlayerTwo) {
          if (isRepeatedMove(playerTwoHistory)) {
            println(s"\nRepeated coordinates! Please play again, Player 2.\n")
            gameState
          } else {
            AttackPhase(
              playerOneCanvas = updateCanvas(playerOneCanvas),
              playerTwoCanvas = playerTwoCanvas,
              activePlayer = PlayerOne,
              playerOneHistory = playerOneHistory,
              playerTwoHistory = registerMove(playerTwoHistory)
            )
          }
        } else {
          gameState
        }
    }
  }
}
