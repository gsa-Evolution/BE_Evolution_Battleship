package local.controller

import local.model.Game.createNewBoard
import local.view.PlacementPhaseView.{playerInstruction, printCanvas, shipsInstruction, switchPlayer, updatedShips}
import local.model.{PlacementPhase, ShipsPlaced}
import scala.io.StdIn.readLine

object IntroGame {
  // Initializes the game and starts the placement phase.
  def initialiseGame: PlacementPhase = {

    // Recursive loop to handle the placement of ships.
    def loop(currentGameState: PlacementPhase): PlacementPhase = {

      // Checks if all ships have been placed.
      if (currentGameState.allShipsPlaced) {
        // Switchs to the next player.
        switchPlayer()

        currentGameState
      } else {
        // Reads input from the player.
        val input = readLine().split(" ")

        try {
          // Validates input length.
          if (input.length != 4) {
            throw new IllegalArgumentException("Invalid input, expected format: [A-J] [1-10] [A-J] [1-10].")
          }

          // Parses input and place the ship.
          val Array(startX, startY, endX, endY) = input
          val updatedGameState = currentGameState.placeShip(startX, startY, endX, endY)

          // Prints the updated game canvas.
          printCanvas(updatedGameState.canvas)

          // Updates the list of placed ships.
          updatedShips(updatedGameState)

          // Provides instructions to the player.
          playerInstruction()

          // Continues the loop with the updated game state.
          loop(updatedGameState)

        } catch {
          case e: IllegalArgumentException =>
            println(e.getMessage)
            loop(currentGameState)
          case e: Exception =>
            println(s"An unexpected error occurred: ${e.getMessage}.")
            loop(currentGameState)
        }
      }
    }

    // Creates a new game board.
    val updatedGameState: PlacementPhase = createNewBoard

    // Prints the initial game canvas.
    printCanvas(updatedGameState.canvas)

    // Provides instructions for placing ships and for the player.
    shipsInstruction()
    playerInstruction()

    // Starts the loop with the initial game state.
    loop(updatedGameState)
  }
}
