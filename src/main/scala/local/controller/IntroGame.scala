package local.controller

import local.model.Game.createNewBoard
import local.view.PlacementPhaseView.{playerInstruction, printCanvas, shipsInstruction, switchPlayer, updatedShips}
import local.model.{PlacementPhase, ShipsPlaced}
import scala.io.StdIn.readLine

object IntroGame {
  // Initializes the game and starts the placement phase
  def initialiseGame: PlacementPhase = {

    // Recursive loop to handle the placement of ships
    def loop(currentGameState: PlacementPhase): PlacementPhase = {

      // Check if all ships have been placed
      if (currentGameState.allShipsPlaced) {
        // Switch to the next player
        switchPlayer()

        currentGameState
      } else {
        // Read input from the player
        val input = readLine().split(" ")

        try {
          // Validate input length
          if (input.length != 4) {
            throw new IllegalArgumentException("Invalid input, expected format: [A-J] [1-10] [A-J] [1-10].")
          }

          // Parse input and place the ship
          val Array(startX, startY, endX, endY) = input
          val updatedGameState = currentGameState.placeShip(startX, startY, endX, endY)

          // Print the updated game canvas
          printCanvas(updatedGameState.canvas)

          // Update the list of placed ships
          updatedShips(updatedGameState)

          // Provide instructions to the player
          playerInstruction()

          // Continue the loop with the updated game state
          loop(updatedGameState)

        } catch {
          // Handle invalid input
          case e: IllegalArgumentException =>
            println(e.getMessage)
            loop(currentGameState)
          // Handle unexpected errors
          case e: Exception =>
            println(s"An unexpected error occurred: ${e.getMessage}.")
            loop(currentGameState)
        }
      }
    }

    // Create a new game board
    val updatedGameState: PlacementPhase = createNewBoard

    // Print the initial game canvas
    printCanvas(updatedGameState.canvas)

    // Provide instructions for placing ships and for the player
    shipsInstruction()
    playerInstruction()

    // Start the loop with the initial game state
    loop(updatedGameState)
  }
}
