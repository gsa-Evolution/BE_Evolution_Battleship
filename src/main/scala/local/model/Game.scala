package local.model

import Canvas._
import Coordinates._

sealed trait ActivePlayer
case object PlayerOne extends ActivePlayer
case object PlayerTwo extends ActivePlayer

case class AttackPhase(
                        playerOneCanvas: Map[(Int, Int), Int],
                        playerTwoCanvas: Map[(Int, Int), Int],
                        activePlayer: ActivePlayer,
                        playerOneHistory: Set[(Int, Int)] = Set(),
                        playerTwoHistory: Set[(Int, Int)] = Set()
                      )

case class PlacementPhase(
                           canvas: Map[(Int, Int), Int] = Map(),
                           shipsPlaced: ShipsPlaced = ShipsPlaced()
                         ) {
  def placeShip(
                 startX: String,
                 startY: String,
                 endX: String,
                 endY: String
                 //gameState: PlacementPhase
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

    ship.updateLocation()
  }
}

object Game {
  def createNewBoard: PlacementPhase = PlacementPhase(canvas = newCanvas)

  def attack(coordinateX: String, coordinateY: String, gameState: AttackPhase): AttackPhase = {
    val x: Int = calculateCoordinates(coordinateX)
    val y: Int = calculateCoordinates(coordinateY)

    def isRepeatedMove(history: Set[(Int, Int)]): Boolean = {
      history.contains((x, y))
    }

    def registerMove(history: Set[(Int, Int)]): Set[(Int, Int)] = {
      history + ((x, y))
    }

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
