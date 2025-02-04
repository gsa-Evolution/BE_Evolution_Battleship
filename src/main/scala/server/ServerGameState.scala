package server

import cats.implicits._
import io.circe.{Codec, Decoder, Encoder, KeyDecoder, KeyEncoder}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredCodec
import local.model.Coordinates
import server.ServerGameState.{AwaitingPlayersServerPhase, BattleshipError, IntroPhase, PlacementServerPhase, WinServerPhase}
import server.ServerWebSockets.{BoardCanvas, PlayerId}


sealed trait ServerGameState {

  def join(playerId: PlayerId): Either[BattleshipError, ServerGameState] =
    this match {
      case AwaitingPlayersServerPhase(players) =>
        val newPlayers = players + playerId
        newPlayers.toList match {
          case firstPlayer :: secondPlayer :: Nil =>
            IntroPhase(
              player1 = firstPlayer,
              player2 = secondPlayer
              //moveNumber = 0,
              //grid = ServerWebSockets.EmptyGrid
            ).asRight

          case _ => AwaitingPlayersServerPhase(newPlayers).asRight
        }

      case inIntro: IntroPhase =>
        if (inIntro.player1 == playerId || inIntro.player2 == playerId) inIntro.asRight
        else BattleshipError.GameNotStarted.asLeft

      case inPlacement: PlacementServerPhase =>
        if (inPlacement.grid != ServerWebSockets.EmptyGrid) inPlacement.asRight
        else BattleshipError.GameAlreadyStarted.asLeft

      case _: WinServerPhase => BattleshipError.GameAlreadyEnded.asLeft
    }

  /*def makeMove(playerId: PlayerId, coordinate: TicTacToe.Coordinate): Either[TicTacToeError, TicTacToe] =
    this match {
      case inProgress @ TicTacToe.InProgress(playerX, playerO, moveNumber, grid) =>
        val movesNow = if (moveNumber % 2 == 0) playerX else playerO

        for {
          _ <- Either.cond(test = movesNow == playerId, left = TicTacToeError.NotYourTurn, right = ())
          _ <- Either.cond(
            test = grid(coordinate.row.value)(coordinate.column.value) == Empty,
            left = TicTacToeError.CellAlreadyTaken,
            right = ()
          )
        } yield {
          val cell    = if (playerId == playerX) TicTacToe.Cell.X else TicTacToe.Cell.O
          val newGrid = {
            val updatedRow = grid(coordinate.row.value).updated(coordinate.column.value, cell)
            grid.updated(coordinate.row.value, updatedRow)
          }

          val isWin = TicTacToe.WinningCombinations.exists { combination =>
            combination.forall { coordinate =>
              newGrid(coordinate.row.value)(coordinate.column.value) == cell
            }
          }

          if (isWin) TicTacToe.Win(winner = playerId, grid = newGrid)
          else {
            val newMoveNumber = moveNumber + 1
            if (newMoveNumber > TicTacToe.MaxMoveNumber) TicTacToe.Tie(newGrid)
            else
              inProgress.copy(
                moveNumber = newMoveNumber,
                grid = newGrid
              )
          }
        }

      case _: TicTacToe.AwaitingPlayersServerPhase => TicTacToeError.GameNotStarted.asLeft
      case _: TicTacToe.Win                        => TicTacToeError.GameAlreadyEnded.asLeft
    }*/
}

object ServerGameState {

  sealed trait BattleshipError

  object BattleshipError {
    case object GameAlreadyStarted extends BattleshipError
    case object GameAlreadyEnded   extends BattleshipError
    case object GameNotStarted     extends BattleshipError
    case object NotYourTurn        extends BattleshipError
    case object CellAlreadyTaken   extends BattleshipError
    case object CellAdjacent       extends BattleshipError
  }

  final case class AwaitingPlayersServerPhase(
                                               players: Set[PlayerId]
                                             ) extends ServerGameState

  final case class IntroPhase(
                               player1: PlayerId,
                               player2: PlayerId
                             ) extends ServerGameState

  final case class PlacementServerPhase(
                               player1: PlayerId,
                               player2: PlayerId,
                               //moveNumber: Int,
                               move: Coordinates, // ?
                               grid: BoardCanvas
                             ) extends ServerGameState

  final case class AttackServerPhase(
                              player1: PlayerId,
                              player2: PlayerId,
                              //moveNumber: Int,
                              grid: BoardCanvas
                            ) extends ServerGameState

  final case class WinServerPhase(
                        winner: PlayerId,
                        grid: BoardCanvas
                      ) extends ServerGameState



  implicit val encodeKeyTupleString: KeyEncoder[(Int, Int)] = new KeyEncoder[(Int, Int)] {
    final def apply(key: (Int, Int)): String = key._1.toString + "," + key._2.toString
  }

  implicit val decodeKeyTupleString: KeyDecoder[(Int, Int)] = new KeyDecoder[(Int, Int)] {
    final def apply(key: String): Option[(Int, Int)] = {
      val pairs = key.split(",")
      Some((pairs(0).toInt, pairs(1).toInt))
    }
  }

  implicit val boardCanvasEncoder: Encoder[BoardCanvas] = Encoder.encodeMap[(Int, Int), Int]
  implicit val boardCanvasDecoder: Decoder[BoardCanvas] = Decoder.decodeMap[(Int, Int), Int]
  implicit val codec: Codec[ServerGameState] = {
    implicit val configuration: Configuration = Configuration.default.withDiscriminator("type")
    deriveConfiguredCodec
  }
}