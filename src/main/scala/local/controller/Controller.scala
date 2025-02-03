package local.controller

import IntroGame.initialiseGame
import PlayGame.playGame
import local.view.PlacementPhaseView.{playerOneWelcomeMessage, playerTwoWelcomeMessage, welcomeMessage}
import local.model.{AttackPhase, PlacementPhase, PlayerOne}

object Controller {
  def apply: Any = {
    welcomeMessage()

    playerOneWelcomeMessage()
    val playerOneState = initialiseGame

    playerTwoWelcomeMessage()
    val playerTwoState = initialiseGame

    playGame(
      AttackPhase(
        playerOneCanvas = playerOneState.canvas,
        playerTwoCanvas = playerTwoState.canvas,
        activePlayer = PlayerOne
      )
    )
  }
}
