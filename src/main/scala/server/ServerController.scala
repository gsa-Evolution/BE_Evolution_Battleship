package server

/*import cats.syntax.all._
import server.ServerGameState.{AwaitingPlayersServerPhase, BattleshipError, PlacementServerPhase, WinServerPhase}
import server.ServerWebSockets.PlayerId*/

//sealed trait ServerController {
//
//  def join(playerId: PlayerId): Either[BattleshipError, ServerController] =
//    this match {
//      case AwaitingPlayersServerPhase(players) =>
//        val newPlayers = players + playerId
//        newPlayers.toList match {
//          case firstPlayer :: secondPlayer :: Nil =>
//              PlacementServerPhase(
//                player1 = firstPlayer,
//                player2 = secondPlayer,
//                moveNumber = 0,
//                grid = ServerWebSockets.EmptyGrid
//              )
//              .asRight
//
//          case _ => AwaitingPlayersServerPhase(newPlayers).asRight
//        }
//
//      case inPlacement: PlacementServerPhase =>
//        if (inPlacement.player1 == playerId || inPlacement.player2 == playerId) inPlacement.asRight
//        else BattleshipError.GameAlreadyStarted.asLeft
//
//      //case
//
//      case _: WinServerPhase => BattleshipError.GameAlreadyEnded.asLeft
//    }
//
//  /*def makeMove(playerId: PlayerId, coordinate: TicTacToe.Coordinate): Either[TicTacToeError, TicTacToe] =
//    this match {
//      case inProgress @ TicTacToe.InProgress(playerX, playerO, moveNumber, grid) =>
//        val movesNow = if (moveNumber % 2 == 0) playerX else playerO
//
//        for {
//          _ <- Either.cond(test = movesNow == playerId, left = TicTacToeError.NotYourTurn, right = ())
//          _ <- Either.cond(
//            test = grid(coordinate.row.value)(coordinate.column.value) == Empty,
//            left = TicTacToeError.CellAlreadyTaken,
//            right = ()
//          )
//        } yield {
//          val cell    = if (playerId == playerX) TicTacToe.Cell.X else TicTacToe.Cell.O
//          val newGrid = {
//            val updatedRow = grid(coordinate.row.value).updated(coordinate.column.value, cell)
//            grid.updated(coordinate.row.value, updatedRow)
//          }
//
//          val isWin = TicTacToe.WinningCombinations.exists { combination =>
//            combination.forall { coordinate =>
//              newGrid(coordinate.row.value)(coordinate.column.value) == cell
//            }
//          }
//
//          if (isWin) TicTacToe.Win(winner = playerId, grid = newGrid)
//          else {
//            val newMoveNumber = moveNumber + 1
//            if (newMoveNumber > TicTacToe.MaxMoveNumber) TicTacToe.Tie(newGrid)
//            else
//              inProgress.copy(
//                moveNumber = newMoveNumber,
//                grid = newGrid
//              )
//          }
//        }
//
//      case _: TicTacToe.AwaitingPlayersServerPhase => TicTacToeError.GameNotStarted.asLeft
//      case _: TicTacToe.Win                        => TicTacToeError.GameAlreadyEnded.asLeft
//    }*/
//}
