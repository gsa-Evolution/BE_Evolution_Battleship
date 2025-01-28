package view

import model.{PlacementPhase, ShipsPlaced}
import model.Game._

object PlacementPhaseView {

  val bold = "\u001b[1m"
  val reset = "\u001b[0m"

  def printCanvas(canvas: Map[(Int, Int), Int]): Unit = println(convertCanvasType(canvas)+ "\n\n")

  def convertCanvasType(canvas: Map[(Int, Int), Int]): String = {
    val topBorder: String = "     " + (for {i <- 'A' to 'J'} yield s"$i").mkString("   ")

    def replaceValue(value: Int): String = value match {
      case 0 => "\u26AA" // Water
      case 1 => "\u26AB" // Cruiser
      case 2 => "\u26AB" // Destroyer
      case 3 => "\u26AB" // Submarine
      case 4 => "\u26AB" // Battleship
      case 5 => "\u26AB" // Carrier
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

  def switchPlayer(): Unit = {
    //Thread.sleep(5000)
    for {_ <- 1 to 15} {
      println("\n")
      //Thread.sleep(900)
    }
  }

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
      ("Destroyer", destroyerStatus, 2),
      ("Cruiser", cruiserStatus, 1)
    )

    shipsStatus.filter(_._2 == true).foreach { case (ship, status, length) =>
      val holeText = if (length == 1) "hole" else "holes"
      println(s"${bold}$ship${reset} was placed successfully! -> ${bold}$length $holeText${reset} \u2705")
    }

    shipsStatus.filter(_._2 == false).foreach { case (ship, status, length) =>
      val holeText = if (length == 1) "hole" else "holes"
      println(s"${bold}$ship${reset} is not placed yet. -> ${bold}$length $holeText${reset} \u274c")
    }
  }

  def playerOneWelcomeMessage(): Unit = println("Player 1, please type 'S' to create your new game board!")

  def playerTwoWelcomeMessage(): Unit = println("Player 2, please type 'S' to create your new game board!")

  def shipsInstruction(): Unit = println(s"Place your boats:\n ${bold}Carrier -> 5 holes\n Battleship -> 4 holes\n Submarine - 3 holes\n Destroyer -> 2 holes\n Cruiser -> 1 hole${reset}")

  def playerInstruction(): Unit = println("\nType 'P [A:J] [1:10] [A:J] [1:10]' -> The first coordinate and the last coordinate of each boat.")

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
