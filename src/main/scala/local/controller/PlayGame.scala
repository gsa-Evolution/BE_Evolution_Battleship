package local.controller

import local.model.Game.attack
import local.view.AttackPhaseView.{playerOneWinMessage, playerTwoWinMessage, printCanvasPostAttack, printCanvasPreAttack}
import local.view.DefendPhaseView.{printCanvasPostDefense, printCanvasPreDefense}
import local.model.{AttackPhase, PlayerOne, PlayerTwo}
import scala.io.StdIn.readLine

object PlayGame {
  val bold = "\u001b[1m"
  val reset = "\u001b[0m"

  // Checks and prints the status of each ship, sunk or not for the current player.
  def checkSunkShips(gameState: AttackPhase) = {
    if (gameState.activePlayer == PlayerOne) {
      // For Player One, checks Player Two's ships.
      val shipsStatus = List(
        ("Destroyer", !gameState.playerTwoCanvas.valuesIterator.exists(v => Set(2).contains(v)), 2),
        ("Cruiser", !gameState.playerTwoCanvas.valuesIterator.exists(v => Set(3).contains(v)), 3),
        ("Submarine", !gameState.playerTwoCanvas.valuesIterator.exists(v => Set(3).contains(v)), 3),
        ("Battleship", !gameState.playerTwoCanvas.valuesIterator.exists(v => Set(4).contains(v)), 4),
        ("Carrier", !gameState.playerTwoCanvas.valuesIterator.exists(v => Set(5).contains(v)), 5)
      )

      // Prints the status for each ship.
      shipsStatus.foreach { case (ship, isSunk, length) =>
        val holeText = if (length == 1) "hole" else "holes"
        val statusIcon = if (isSunk) "\u2705" else "\u274c"
        val statusText = if (isSunk) "was sunk successfully!" else "is not sunk yet."
        println(s"${bold}$ship${reset} $statusText -> ${bold}$length $holeText${reset} $statusIcon")
      }
    }
    else {
      // For Player Two, checks Player One's ships.
      val shipsStatus = List(
        ("Destroyer", !gameState.playerOneCanvas.valuesIterator.exists(v => Set(2).contains(v)), 2),
        ("Cruiser", !gameState.playerOneCanvas.valuesIterator.exists(v => Set(3).contains(v)), 3),
        ("Submarine", !gameState.playerOneCanvas.valuesIterator.exists(v => Set(3).contains(v)), 3),
        ("Battleship", !gameState.playerOneCanvas.valuesIterator.exists(v => Set(4).contains(v)), 4),
        ("Carrier", !gameState.playerOneCanvas.valuesIterator.exists(v => Set(5).contains(v)), 5)
      )

      // Prints the status for each ship.
      shipsStatus.foreach { case (ship, isSunk, length) =>
        val holeText = if (length == 1) "hole" else "holes"
        val statusIcon = if (isSunk) "\u2705" else "\u274c"
        val statusText = if (isSunk) "was sunk successfully!" else "is not sunk yet."
        println(s"${bold}$ship${reset} $statusText -> ${bold}$length $holeText${reset} $statusIcon")
      }
    }
  }

  def playGame(gameState: AttackPhase): Any = {
    // Checks if either player has won.
    val playerOneWins: Boolean = !gameState.playerTwoCanvas.valuesIterator.exists(v => Set(1, 2, 3, 4, 5).contains(v))
    val playerTwoWins: Boolean = !gameState.playerOneCanvas.valuesIterator.exists(v => Set(1, 2, 3, 4, 5).contains(v))

    if (playerOneWins) playerOneWinMessage()
    else if (playerTwoWins) playerTwoWinMessage()
    // If no winner, continues with the current player's turn.
    else if (gameState.activePlayer == PlayerOne) {
      checkSunkShips(gameState)
      printCanvasPreAttack(gameState.playerTwoCanvas, activePlayer = gameState.activePlayer)

      // Reads attack coordinates from Player One.
      val input = readLine().split(" ")

      try {
        if (input.length != 2) {
          throw new IllegalArgumentException("Invalid input, expected format: [A-J] [1-10].")
        }

        val Array(xCoordinate, yCoordinate) = input
        // Performs the attack and update the game state.
        val updatedGameState = attack(xCoordinate, yCoordinate, gameState)

        // Prints the result of the attack and updated boards.
        println("PLAYER 1 VIEW:")
        println(f"Last Player 1's play: ${bold}${xCoordinate.toUpperCase} $yCoordinate${reset}")
        println("____________________________________________")
        println("Player 2 attacking board:")
        printCanvasPostAttack(updatedGameState.playerTwoCanvas)
        println("Player 1 defending board:")
        printCanvasPostDefense(updatedGameState.playerOneCanvas)
        println("____________________________________________")

        // Continues the game loop with the updated state.
        playGame(updatedGameState)

      } catch {
        case e: IllegalArgumentException =>
          println(e.getMessage)
          playGame(gameState)
        case e: Exception =>
          println(s"An unexpected error occurred: ${e.getMessage}.")
          playGame(gameState)
      }
    } else if (gameState.activePlayer == PlayerTwo) {
      checkSunkShips(gameState)
      printCanvasPreAttack(gameState.playerOneCanvas, activePlayer = gameState.activePlayer)

      // Reads attack coordinates from Player Two.
      val input = readLine().split(" ")

      try {
        if (input.length != 2) {
          throw new IllegalArgumentException("Invalid input, expected format: [A-J] [1-10].")
        }

        val Array(xCoordinate, yCoordinate) = input
        // Performs the attack and update the game state.
        val updatedGameState = attack(xCoordinate, yCoordinate, gameState)

        // Prints the result of the attack and updated boards.
        println("PLAYER 2 VIEW:")
        println(f"Last Player 2's play: ${bold}${xCoordinate.toUpperCase} $yCoordinate${reset}")
        println("____________________________________________")
        println("Player 1 attacking board:")
        printCanvasPostAttack(updatedGameState.playerOneCanvas)
        println("Player 2 defending board:")
        printCanvasPostDefense(updatedGameState.playerTwoCanvas)
        println("____________________________________________")

        // Continues the game loop with the updated state.
        playGame(updatedGameState)

      } catch {
        case e: IllegalArgumentException =>
          println(e.getMessage)
          playGame(gameState)
        case e: Exception =>
          println(s"An unexpected error occurred: ${e.getMessage}.")
          playGame(gameState)
      }
    } else println("error")
  }
}
