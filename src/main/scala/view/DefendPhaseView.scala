package view

import model.{ActivePlayer, PlayerOne, PlayerTwo}

object DefendPhaseView {
  def printCanvasPreDefense(canvas: Map[(Int, Int), Int], activePlayer: ActivePlayer): Unit = {
    println(convertCanvasTypeDefend(canvas) + "\n\n")

    /*activePlayer match {
      case PlayerOne => println("\nPlayer 1, type 'A [A:J] [1:10]' -> The coordinate you want to attack!")
      case PlayerTwo => println("\nlayer 2, type 'A [A:J] [1:10]' -> The coordinate you want to attack!")
      case _ => println("error")
    }*/
  }

  def printCanvasPostDefense(canvas: Map[(Int, Int), Int]): Unit = {
    println(convertCanvasTypeDefend(canvas))
    //Thread.sleep(5000)
    //println("\n\n")
  }

  def convertCanvasTypeDefend(canvas: Map[(Int, Int), Int]): String = {
    val topBorder: String = "     " + (for {i <- 'A' to 'J'} yield s"$i").mkString("   ")

    val row: IndexedSeq[String] = for {
      i <- 0 to 9
    } yield "\u0020\u0020" + (for {j <- 0 to 9} yield canvas(j, i)).mkString("\u0020\u0020")

    val anonymisedRowDefender: IndexedSeq[String] =
      row.map {
        row =>
          row
            .replace("0", "\u26AA") // Water
            .replace("1", "\u26AB") // Cruiser
            .replace("2", "\u26AB") // Destroyer
            .replace("3", "\u26AB") // Submarine
            .replace("4", "\u26AB") // Battleship
            .replace("5", "\u26AB") // Cruiser
            .replace("6", "\uD83D\uDD34") // Hit ship
            .replace("7", "\uD83D\uDD35") // Hit water
      }

    val middleRows: String = (for {
      i <- 0 to 8
    } yield s"\u0020${anonymisedRowDefender.zipWithIndex(i)._2 + 1}${anonymisedRowDefender.zipWithIndex(i)._1}").mkString("\n", "\n", "\n")

    val lastRow: String = s"${anonymisedRowDefender.zipWithIndex(9)._2 + 1}${anonymisedRowDefender.zipWithIndex(9)._1}"

    topBorder + middleRows + lastRow
  }
}
