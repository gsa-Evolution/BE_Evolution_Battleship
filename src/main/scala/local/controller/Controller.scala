package local.controller

import IntroGame.initialiseGame
import PlayGame.playGame
import local.view.PlacementPhaseView.{playerOneWelcomeMessage, playerTwoWelcomeMessage, welcomeMessage}
import local.model.{AttackPhase, PlacementPhase, PlayerOne}

object Controller {
  // Entry point for running the game locally.
  def apply: Any = {
    welcomeMessage()

    // Player one intro phase.
    playerOneWelcomeMessage()
    val playerOneState = initialiseGame

    // Player two intro phase.
    playerTwoWelcomeMessage()
    val playerTwoState = initialiseGame

    // Startz the attack phase with both players' boards and set PlayerOne as the first to play.
    playGame(
      AttackPhase(
        playerOneCanvas = playerOneState.canvas,
        playerTwoCanvas = playerTwoState.canvas,
        activePlayer = PlayerOne
      )
    )
  }
}
