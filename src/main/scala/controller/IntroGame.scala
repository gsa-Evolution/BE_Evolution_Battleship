package controller

import model.Game.{createNewBoard, placeShip}
import model.{PlacementPhase, ShipsPlaced}
import view.PlacementPhaseView.{playerInstruction, printCanvas, shipsInstruction, switchPlayer, updatedShips}
import scala.io.StdIn.readLine

object IntroGame {
  def initialiseGame: PlacementPhase = {

    def loop(currentGameState: PlacementPhase): PlacementPhase = {
      val allShipsPlaced =
        currentGameState.shipsPlaced.submarine &&
          currentGameState.shipsPlaced.destroyer &&
          currentGameState.shipsPlaced.cruiser &&
          currentGameState.shipsPlaced.battleship &&
          currentGameState.shipsPlaced.carrier

      if (allShipsPlaced) {
        switchPlayer()

        currentGameState
      } else {
        val input = readLine().split(" ")

        try {
          if (input.length != 4) {
            throw new IllegalArgumentException("Invalid input, expected format: [A-J] [1-10] [A-J] [1-10].")
          }

          val Array(startX, startY, endX, endY) = input
          val updatedGameState = placeShip(startX, startY, endX, endY, currentGameState)

          printCanvas(updatedGameState.canvas)

          updatedShips(updatedGameState)

          playerInstruction()

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

    val updatedGameState: PlacementPhase = createNewBoard

    printCanvas(updatedGameState.canvas)

    shipsInstruction()
    playerInstruction()

    loop(updatedGameState)
  }
}
