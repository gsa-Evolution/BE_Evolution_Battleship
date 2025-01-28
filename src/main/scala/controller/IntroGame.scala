package controller

import model.Game.{createNewBoard, placeShip}
import model.{PlacementPhase, ShipsPlaced}
import view.PlacementPhaseView.{playerInstruction, printCanvas, shipsInstruction, switchPlayer, updatedShips}
import model.ships._

import scala.annotation.tailrec
import scala.io.StdIn.readLine

object IntroGame {
  @tailrec def initialiseGame(currentGameState: PlacementPhase): PlacementPhase = {
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

      input.head match {
        //create new board
        case "S" | "s" =>

          val updatedGameState = createNewBoard

          printCanvas(updatedGameState.canvas)

          shipsInstruction()
          playerInstruction()

          initialiseGame(updatedGameState)

        //place ship
        case "P" | "p" =>
          val Array(_, startX, startY, endX, endY) = input
          val updatedGameState = placeShip(startX, startY, endX, endY, currentGameState)

          printCanvas(updatedGameState.canvas)

          updatedShips(updatedGameState)

          playerInstruction()

          initialiseGame(updatedGameState)

        //in all other cases, try again
        case _ => initialiseGame(currentGameState)
      }
    }
  }
}
