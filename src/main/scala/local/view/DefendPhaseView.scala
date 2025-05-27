package local.view

import local.model.ActivePlayer

object DefendPhaseView {
  // Prints the defense board before the player makes a move.
  def printCanvasPreDefense(canvas: Map[(Int, Int), Int], activePlayer: ActivePlayer): Unit = {
    println(convertCanvasTypeDefend(canvas) + "\n\n")
  }

  // Prints the defense board after the player has made a move.
  def printCanvasPostDefense(canvas: Map[(Int, Int), Int]): Unit = {
    println(convertCanvasTypeDefend(canvas))
  }

  // Converts the board state to a string for display, showing ship types for the defender.
  def convertCanvasTypeDefend(canvas: Map[(Int, Int), Int]): String = {
    val topBorder: String = "     " + (for {i <- 'A' to 'J'} yield s"$i").mkString("   ")

    val row: IndexedSeq[String] = for {
      i <- 0 to 9
    } yield "\u0020\u0020" + (for {j <- 0 to 9} yield canvas(j, i)).mkString("\u0020\u0020")

    // Replaces cell values with icons for the defender's view.
    val anonymisedRowDefender: IndexedSeq[String] =
      row.map {
        row =>
          row
            .replace("0", "\u26AA") // Water.
            .replace("1", "\u26AB") // Cruiser.
            .replace("2", "\u26AB") // Destroyer.
            .replace("3", "\u26AB") // Submarine.
            .replace("4", "\u26AB") // Battleship.
            .replace("5", "\u26AB") // Carrier.
            .replace("6", "\uD83D\uDD34") // Hit ship.
            .replace("7", "\uD83D\uDD35") // Hit water.
      }

    val middleRows: String = (for {
      i <- 0 to 8
    } yield s"\u0020${anonymisedRowDefender.zipWithIndex(i)._2 + 1}${anonymisedRowDefender.zipWithIndex(i)._1}").mkString("\n", "\n", "\n")

    val lastRow: String = s"${anonymisedRowDefender.zipWithIndex(9)._2 + 1}${anonymisedRowDefender.zipWithIndex(9)._1}"

    topBorder + middleRows + lastRow
  }
}
