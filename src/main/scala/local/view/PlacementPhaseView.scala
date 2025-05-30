package local.view

import local.model.PlacementPhase

object PlacementPhaseView {
  val bold = "\u001b[1m"
  val reset = "\u001b[0m"

  // Prints the current board for ship placement.
  def printCanvas(canvas: Map[(Int, Int), Int]): Unit = println(convertCanvasType(canvas)+ "\n\n")

  // Converts the board state to a string for display, showing ship types for the player.
  def convertCanvasType(canvas: Map[(Int, Int), Int]): String = {
    val topBorder: String = "     " + (for {i <- 'A' to 'J'} yield s"$i").mkString("   ")

    // Converts cell values to display icons.
    def replaceValue(value: Int): String = value match {
      case 0 => "\u26AA" // Water.
      case 1 => "\u26AB" // Cruiser.
      case 2 => "\u26AB" // Destroyer.
      case 3 => "\u26AB" // Submarine.
      case 4 => "\u26AB" // Battleship.
      case 5 => "\u26AB" // Carrier.
      case _ => "?"
    }

    val row: IndexedSeq[String] = for {
      i <- 0 to 9
    } yield "\u0020\u0020" + (for {j <- 0 to 9} yield replaceValue(canvas(j, i))).mkString("\u0020\u0020")

    val middleRows: String = (for {
      i <- 0 to 8
    } yield s"\u0020${row.zipWithIndex(i)._2 + 1}${row.zipWithIndex(i)._1}").mkString("\n", "\n", "\n")

    val lastRow: String = s"${row.zipWithIndex(9)._2 + 1}${row.zipWithIndex(9)._1}"

    topBorder + middleRows + lastRow
  }

  // Clears the console to switch to the next player.
  def switchPlayer(): Unit = {
    for {_ <- 1 to 15} {
      println("\n")
    }
  }

  // Prints the status of each ship, placed or not, after each placement.
  def updatedShips(gameState: PlacementPhase): Unit = {
    val currentShipsPlaced = gameState.shipsPlaced

    val carrierStatus = currentShipsPlaced.carrier
    val battleshipStatus = currentShipsPlaced.battleship
    val submarineStatus = currentShipsPlaced.submarine
    val destroyerStatus = currentShipsPlaced.destroyer
    val cruiserStatus = currentShipsPlaced.cruiser

    val shipsStatus = List(
      ("Carrier", carrierStatus, 5),
      ("Battleship", battleshipStatus, 4),
      ("Submarine", submarineStatus, 3),
      ("Cruiser", cruiserStatus, 3),
      ("Destroyer", destroyerStatus, 2)
    )

    // Prints status for ships that have been placed.
    shipsStatus.filter(_._2 == true).foreach { case (ship, status, length) =>
      val holeText = if (length == 1) "hole" else "holes"
      println(s"${bold}$ship${reset} was placed successfully! -> ${bold}$length $holeText${reset} \u2705")
    }

    // Prints status for ships that have not been placed yet.
    shipsStatus.filter(_._2 == false).foreach { case (ship, status, length) =>
      val holeText = if (length == 1) "hole" else "holes"
      println(s"${bold}$ship${reset} is not placed yet. -> ${bold}$length $holeText${reset} \u274c")
    }
  }

  // Prints welcome message for Player 1.
  def playerOneWelcomeMessage(): Unit = println("Player 1, it is your turn to place your ships according to the list!")

  // Prints welcome message for Player 2.
  def playerTwoWelcomeMessage(): Unit = println("Player 2, it is your turn to place your ships according to the list!")

  // Prints instructions for placing ships.
  def shipsInstruction(): Unit = println(s"Place your ships:\n ${bold}Carrier -> 5 holes\n Battleship -> 4 holes\n Submarine - 3 holes\n Destroyer -> 2 holes\n Cruiser -> 1 hole${reset}")

  // Prints instructions for entering ship coordinates.
  def playerInstruction(): Unit = println("\nType the first coordinate and the last coordinate of each ship. -> [A:J] [1:10] [A:J] [1:10]:")

  // Prints the welcome message.
  def welcomeMessage(): Unit = println(title)

  private val title: String =
    """
      | __        __   _                            _          _   _
      | \ \      / ___| | ___ ___  _ __ ___   ___  | |_ ___   | |_| |__   ___
      |  \ \ /\ / / _ | |/ __/ _ \| '_ ` _ \ / _ \ | __/ _ \  | __| '_ \ / _ \
      |   \ V  V |  __| | (_| (_) | | | | | |  __/ | || (_) | | |_| | | |  __/
      |    \_/\_/ \___|_|\___\___/|_| |_| |_|\___|  \__\___/   \__|_| |_|\___|
      |
      |▗▄▄▄▖▗▖  ▗▖ ▗▄▖ ▗▖   ▗▖ ▗▖▗▄▄▄▖▗▄▄▄▖ ▗▄▖ ▗▖  ▗▖
      |▐▌   ▐▌  ▐▌▐▌ ▐▌▐▌   ▐▌ ▐▌  █    █  ▐▌ ▐▌▐▛▚▖▐▌
      |▐▛▀▀▘▐▌  ▐▌▐▌ ▐▌▐▌   ▐▌ ▐▌  █    █  ▐▌ ▐▌▐▌ ▝▜▌
      |▐▙▄▄▖ ▝▚▞▘ ▝▚▄▞▘▐▙▄▄▖▝▚▄▞▘  █  ▗▄█▄▖▝▚▄▞▘▐▌  ▐▌
      |▗▄▄▖  ▗▄▖▗▄▄▄▖▗▄▄▄▖▗▖   ▗▄▄▄▖ ▗▄▄▖▗▖ ▗▖▗▄▄▄▖▗▄▄▖
      |▐▌ ▐▌▐▌ ▐▌ █    █  ▐▌   ▐▌   ▐▌   ▐▌ ▐▌  █  ▐▌ ▐▌
      |▐▛▀▚▖▐▛▀▜▌ █    █  ▐▌   ▐▛▀▀▘ ▝▀▚▖▐▛▀▜▌  █  ▐▛▀▘
      |▐▙▄▞▘▐▌ ▐▌ █    █  ▐▙▄▄▖▐▙▄▄▖▗▄▄▞▘▐▌ ▐▌▗▄█▄▖▐▌
      |                             _
      |   __ _  __ _ _ __ ___   ___| |
      |  / _` |/ _` | '_ ` _ \ / _ | |
      | | (_| | (_| | | | | | |  __|_|
      |  \__, |\__,_|_| |_| |_|\___(_)
      |  |___/
      |
      |""".stripMargin + "\n\n\n"
}
