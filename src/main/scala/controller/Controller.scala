package controller

import controller.IntroGame.initialiseGame
import controller.PlayGame.playGame
import model.{AttackPhase, PlacementPhase, PlayerOne}
import view.PlacementPhaseView.{playerOneWelcomeMessage, playerTwoWelcomeMessage, welcomeMessage}

object Controller {
  def apply: Any = {
    welcomeMessage()

    playerOneWelcomeMessage()
    val playerOneState = initialiseGame(currentGameState = PlacementPhase())

    playerTwoWelcomeMessage()
    val playerTwoState = initialiseGame(currentGameState = PlacementPhase())

    playGame(
      AttackPhase(
        playerOneCanvas = playerOneState.canvas,
        playerTwoCanvas = playerTwoState.canvas,
        activePlayer = PlayerOne
      )
    )
  }
}
