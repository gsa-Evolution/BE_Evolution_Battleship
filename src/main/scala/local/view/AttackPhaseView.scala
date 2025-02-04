package local.view

import local.model.{ActivePlayer, PlayerOne, PlayerTwo}

object AttackPhaseView {
  def printCanvasPreAttack(canvas: Map[(Int, Int), Int], activePlayer: ActivePlayer): Unit = {
    println("\n\n" + convertCanvasTypeAttack(canvas) + "\n\n")

    activePlayer match {
      case PlayerOne => println("\nPlayer 1, type '[A:J] [1:10]' -> The coordinate you want to attack:")
      case PlayerTwo => println("\nPlayer 2, type '[A:J] [1:10]' -> The coordinate you want to attack:")
      case _ => println("error")
    }
  }

  def printCanvasPostAttack(canvas: Map[(Int, Int), Int]): Unit = {
    println(convertCanvasTypeAttack(canvas))
    //Thread.sleep(5000)
    //println("\n\n")
  }

  def convertCanvasTypeAttack(canvas: Map[(Int, Int), Int]): String = {
    val topBorder: String = "     " + (for {i <- 'A' to 'J'} yield s"$i").mkString("   ")

    val row: IndexedSeq[String] = for {
      i <- 0 to 9
    } yield "\u0020\u0020" + (for {j <- 0 to 9} yield canvas(j, i)).mkString("\u0020\u0020")

    val anonymisedRowAtacker: IndexedSeq[String] =
      row.map {
        row =>
          row
            .replace("0", "\u26AA") // Water
            .replace("1", "\u26AA") // Cruiser
            .replace("2", "\u26AA") // Destroyer
            .replace("3", "\u26AA") // Submarine
            .replace("4", "\u26AA") // Battleship
            .replace("5", "\u26AA") // Carrier
            .replace("6", "\uD83D\uDD34") // Hit ship
            .replace("7", "\uD83D\uDD35") // Hit water
      }

    val middleRows: String = (for {
      i <- 0 to 8
    } yield s"\u0020${anonymisedRowAtacker.zipWithIndex(i)._2 + 1}${anonymisedRowAtacker.zipWithIndex(i)._1}").mkString("\n", "\n", "\n")

    val lastRow: String = s"${anonymisedRowAtacker.zipWithIndex(9)._2 + 1}${anonymisedRowAtacker.zipWithIndex(9)._1}"

    topBorder + middleRows + lastRow
  }

  def playerOneWinMessage(): Unit = println(playerOneWins)

  def playerTwoWinMessage(): Unit = println(playerTwoWins)

  private val playerOneWins: String =
    """
      |.______    __          ___   ____    ____  _______ .______          __     ____    __    ____  __  .__   __.      _______. __
      ||   _  \  |  |        /   \  \   \  /   / |   ____||   _  \        /_ |    \   \  /  \  /   / |  | |  \ |  |     /       ||  |
      ||  |_)  | |  |       /  ^  \  \   \/   /  |  |__   |  |_)  |        | |     \   \/    \/   /  |  | |   \|  |    |   (----`|  |
      ||   ___/  |  |      /  /_\  \  \_    _/   |   __|  |      /         | |      \            /   |  | |  . `  |     \   \    |  |
      ||  |      |  `----./  _____  \   |  |     |  |____ |  |\  \----.    | |       \    /\    /    |  | |  |\   | .----)   |   |__|
      || _|      |_______/__/     \__\  |__|     |_______|| _| `._____|    |_|        \__/  \__/     |__| |__| \__| |_______/    (__)
      |""".stripMargin

  private val playerTwoWins: String =
    """
      |.______    __          ___   ____    ____  _______ .______          ___      ____    __    ____  __  .__   __.      _______. __
      ||   _  \  |  |        /   \  \   \  /   / |   ____||   _  \        |__ \     \   \  /  \  /   / |  | |  \ |  |     /       ||  |
      ||  |_)  | |  |       /  ^  \  \   \/   /  |  |__   |  |_)  |          ) |     \   \/    \/   /  |  | |   \|  |    |   (----`|  |
      ||   ___/  |  |      /  /_\  \  \_    _/   |   __|  |      /          / /       \            /   |  | |  . `  |     \   \    |  |
      ||  |      |  `----./  _____  \   |  |     |  |____ |  |\  \----.    / /_        \    /\    /    |  | |  |\   | .----)   |   |__|
      || _|      |_______/__/     \__\  |__|     |_______|| _| `._____|   |____|        \__/  \__/     |__| |__| \__| |_______/    (__)
      |""".stripMargin
}
