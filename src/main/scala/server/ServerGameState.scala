package server

import cats.implicits._
import io.circe.{Codec, Decoder, Encoder, KeyDecoder, KeyEncoder}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredCodec
import local.model.{Coordinates, PlacementPhase, ShipsPlaced}
import server.ServerGameState.{AttackServerPhase, AwaitingPlayersServerPhase, BattleshipError, PlacementServerPhase, WinServerPhase}
import server.ServerWebSockets.{BoardCanvas, EmptyGrid, PlayerId}


sealed trait ServerGameState {

  def join(playerId: PlayerId): Either[BattleshipError, ServerGameState] =
    this match {
      case AwaitingPlayersServerPhase(players) =>
        val newPlayers = players + playerId
        val newBoard: PlacementPhase = PlacementPhase(
          canvas = EmptyGrid,
          shipsPlaced = ShipsPlaced()
        )
        newPlayers.toList match {
          case firstPlayer :: secondPlayer :: Nil =>
            PlacementServerPhase(
              boards = Map(
                firstPlayer -> newBoard,
                secondPlayer -> newBoard
              )
            ).asRight

          case _ => AwaitingPlayersServerPhase(newPlayers).asRight
        }
      case _ => BattleshipError.GameAlreadyStarted.asLeft
    }

  def makePlacement(playerId: PlayerId, coordinate: List[Coordinates]): Either[BattleshipError, ServerGameState] =
    this match {
      case PlacementServerPhase(boards) =>
        val allPlaced = boards.exists { case (playerId: PlayerId, placementPhase) => placementPhase.allShipsPlaced }
        val updatedBoard = PlacementPhase.placeShip(coordinate) // chamar a função que atualiza o board

        if (allPlaced) AttackServerPhase().asRight//PlacementServerPhase(boards).asRight
        else PlacementServerPhase(updatedBoard) //BattleshipError.GameAlreadyStarted.asLeft

      //PlacementServerPhase(player1, player2, map)
      case PlacementServerPhase @ PlacementServerPhase(player1, player2, moveNumber, grid) =>
        // placement(coord, board): board
        // playerId = true
        // se true and true ent retorna o estado attack
        // se true and false retorna estado phament com updated map
        val movesNow = if (moveNumber % 2 == 0) player1 else player2

          /*val isWin = TicTacToe.WinningCombinations.exists { combination =>
            combination.forall { coordinate =>
              newGrid(coordinate.row.value)(coordinate.column.value) == cell
            }
          }*/
        }

      case _: ServerGameState.AwaitingPlayersServerPhase => BattleshipError.GameNotStarted.asLeft
      case _: ServerGameState.WinServerPhase             => BattleshipError.GameAlreadyEnded.asLeft
    }

  def makeAttack(pplayerID, cordenadas) =
    this match {
    // ataca
      // se alguem ganhou, se sim, retorna o WIN
      // senao, retornar ATACKPHASE updated


    if (isWin) ServerGameState.Win(winner = playerId, grid = newGrid)
    else {
    val newMoveNumber = moveNumber + 1
    if (newMoveNumber > ServerGameState.MaxMoveNumber) ServerGameState.Tie(newGrid)
    else
    inProgress.copy(
    moveNumber = newMoveNumber,
    grid = newGrid
    )
    }

      case _: WinServerPhase => BattleshipError.GameAlreadyEnded.asLeft
    }
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

  // criar um PlacementPhase vazio para os dois DONE
  // player1 -> PlacementPhase1; player2 -> PlacementPhase2 DONE

  // player1 - request placement com todos
  // verificar o request e atuakizar PlacementPhase1

  // player1 - request placement com todos
  // nao aceitar se o placement ja esta feito


  // player2 - request placement com todos
  // verificar o request e atuakizar PlacementPhase2
  // player1 -> PlacementPhase1atu; player2 -> PlacementPhase2atu
  // Se os dois tiverem atualizados ent muda para atack



  final case class PlacementServerPhase(
                                       boards: Map[PlayerId, PlacementPhase]
                               // player1: PlayerId,
                               // player2: PlayerId,
                               //moveNumber: Int,
                               // move: Coordinates, // ?
                               // grid: BoardCanvas
                             ) extends ServerGameState

  // player - request atack
  // executa atack -> verificar se ganhou ou nao, se sim mudar para o estado Win e broadcast para todos

  // player - request placement
  // rejeitar/dar erro

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