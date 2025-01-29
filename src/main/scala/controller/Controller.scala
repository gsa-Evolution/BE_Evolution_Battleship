package controller

import controller.IntroGame.initialiseGame
import controller.PlayGame.playGame
import model.Game.createNewBoard
import model.{AttackPhase, PlacementPhase, PlayerOne}
import view.PlacementPhaseView.{playerOneWelcomeMessage, playerTwoWelcomeMessage, welcomeMessage}

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
