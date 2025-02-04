package server

import cats.syntax.all._
import cats.effect.std.Queue
import cats.effect.{IO, Ref, Resource, ResourceApp}
import com.comcast.ip4s.IpLiteralSyntax
import io.circe.syntax.EncoderOps
import io.circe._
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import server.ServerGameState._
import java.util.UUID

object ServerWebSockets extends ResourceApp.Forever {
  type PlayerId = String
  type BoardCanvas = Map[(Int, Int), Int]
  val EmptyGrid: BoardCanvas = {
    Map(
      (0, 0) -> 0, (1, 0) -> 0, (2, 0) -> 0, (3, 0) -> 0, (4, 0) -> 0, (5, 0) -> 0, (6, 0) -> 0, (7, 0) -> 0, (8, 0) -> 0, (9, 0) -> 0,
      (0, 1) -> 0, (1, 1) -> 0, (2, 1) -> 0, (3, 1) -> 0, (4, 1) -> 0, (5, 1) -> 0, (6, 1) -> 0, (7, 1) -> 0, (8, 1) -> 0, (9, 1) -> 0,
      (0, 2) -> 0, (1, 2) -> 0, (2, 2) -> 0, (3, 2) -> 0, (4, 2) -> 0, (5, 2) -> 0, (6, 2) -> 0, (7, 2) -> 0, (8, 2) -> 0, (9, 2) -> 0,
      (0, 3) -> 0, (1, 3) -> 0, (2, 3) -> 0, (3, 3) -> 0, (4, 3) -> 0, (5, 3) -> 0, (6, 3) -> 0, (7, 3) -> 0, (8, 3) -> 0, (9, 3) -> 0,
      (0, 4) -> 0, (1, 4) -> 0, (2, 4) -> 0, (3, 4) -> 0, (4, 4) -> 0, (5, 4) -> 0, (6, 4) -> 0, (7, 4) -> 0, (8, 4) -> 0, (9, 4) -> 0,
      (0, 5) -> 0, (1, 5) -> 0, (2, 5) -> 0, (3, 5) -> 0, (4, 5) -> 0, (5, 5) -> 0, (6, 5) -> 0, (7, 5) -> 0, (8, 5) -> 0, (9, 5) -> 0,
      (0, 6) -> 0, (1, 6) -> 0, (2, 6) -> 0, (3, 6) -> 0, (4, 6) -> 0, (5, 6) -> 0, (6, 6) -> 0, (7, 6) -> 0, (8, 6) -> 0, (9, 6) -> 0,
      (0, 7) -> 0, (1, 7) -> 0, (2, 7) -> 0, (3, 7) -> 0, (4, 7) -> 0, (5, 7) -> 0, (6, 7) -> 0, (7, 7) -> 0, (8, 7) -> 0, (9, 7) -> 0,
      (0, 8) -> 0, (1, 8) -> 0, (2, 8) -> 0, (3, 8) -> 0, (4, 8) -> 0, (5, 8) -> 0, (6, 8) -> 0, (7, 8) -> 0, (8, 8) -> 0, (9, 8) -> 0,
      (0, 9) -> 0, (1, 9) -> 0, (2, 9) -> 0, (3, 9) -> 0, (4, 9) -> 0, (5, 9) -> 0, (6, 9) -> 0, (7, 9) -> 0, (8, 9) -> 0, (9, 9) -> 0
    )
  }

  final case class GameRoom(
                             state: ServerGameState,
                             webSockets: Map[PlayerId, Queue[IO, WebSocketFrame.Text]]
  ) {
    def handleCommand(c: ServerCommand): (GameRoom, Either[ServerGameState.BattleshipError, ServerGameState]) = {
      val newStateOrError =
        for {
          updatedState <- c match {
            case ServerCommand.Join(playerId)                 => state.join(playerId)
            //case ServerCommand.MakeMove(playerId, coordinate) => state.makeMove(playerId, coordinate)
          }
        } yield copy(state = updatedState)

      (newStateOrError.getOrElse(this), newStateOrError.map(_.state))
    }
  }

  override def run(args: List[String]): Resource[IO, Unit] = {
    for {
      gameRooms <- Ref.of[IO, Map[UUID, Ref[IO, GameRoom]]](Map.empty).toResource
      _ <- EmberServerBuilder
        .default[IO]
        .withHost(host"localhost")
        .withPort(port"8000")
        .withHttpWebSocketApp(QueueRoutes(gameRooms, _).orNotFound)
        .build
    } yield ()
  }

  private object QueueRoutes {
    def apply(
               gameRooms: Ref[IO, Map[UUID, Ref[IO, GameRoom]]],
               webSocketBuilder: WebSocketBuilder2[IO]
             ): HttpRoutes[IO] =

      HttpRoutes.of[IO] {
        case POST -> Root / "createGame" =>
          for {
            id <- IO.randomUUID
            roomRef <- Ref.of[IO, GameRoom](
              GameRoom(
                state = AwaitingPlayersServerPhase(Set.empty),
                webSockets = Map.empty
              )
            )
            _ <- gameRooms.update(_.updated(id, roomRef))
            response <- Created(id.toString)
          } yield response

        case GET -> Root / "join" / roomId / playerId =>
          for {
            id            <- IO(UUID.fromString(roomId))
            maybeStateRef <- gameRooms.get.map(_.get(id))
            response      <- maybeStateRef match {
              case Some(stateRef) =>
                for {
                  queue    <- Queue.unbounded[IO, WebSocketFrame.Text]
                  result   <- stateRef.modify { _.handleCommand(ServerCommand.Join(playerId)) }
                  _        <- if (result.isRight) stateRef.update { room =>
                    room.copy(webSockets = room.webSockets.updated(playerId, queue))
                  }
                  else IO.unit
                  response <- result match {
                    case Left(error)     => BadRequest(s"Failed to join room $id: $error")
                    case Right(newState) =>
                      val newStateMsg = WebSocketFrame.Text(newState.asJson.noSpaces)
                      for {
                        response <- webSocketBuilder.build(
                          send = fs2.Stream.fromQueueUnterminated(queue),
                          receive = _.foreach {
                            case text: WebSocketFrame.Text =>
                              for {
                                request         <-
                                  IO.fromEither(jawn.decode[ServerRequest](text.str))
                                command          = request match {
                                  case ServerRequest.MakeMove(coordinate) =>
                                    ServerCommand.MakeMove(playerId, coordinate)
                                }
                                newStateOrError <- stateRef.modify {
                                  _.handleCommand(command)
                                }

                                /// newState
                                ///

                                _ <- newStateOrError match {
                                  case Left(e) =>
                                    queue.offer(WebSocketFrame.Text(e.toString))

                                  case Right(newState) =>
                                    //

                                    def sendToPlayer(
                                                      playerId: PlayerId,
                                                      msg: Json
                                                    ) =
                                      for {
                                        room             <- stateRef.get
                                        maybePlayersQueue =
                                          room.webSockets.get(playerId)
                                        _                <-
                                          maybePlayersQueue.traverse_ { q =>
                                            q.offer(
                                              WebSocketFrame.Text(msg.noSpaces)
                                            )
                                          }
                                      } yield ()

                                    stateRef.get
                                      .map(room => room.webSockets.values.toList)
                                      .flatMap(webSockets =>
                                        webSockets.traverse(
                                          _.offer(
                                            WebSocketFrame
                                              .Text(newState.asJson.noSpaces)
                                          )
                                        )
                                      )
                                }
                              } yield ()

                            case _ => IO.unit
                          }
                        )
                        _        <- stateRef.get
                          .map(_.webSockets.values.toList)
                          .flatMap(_.traverse(_.offer(newStateMsg)))
                      } yield response
                  }
                } yield response

              case None => NotFound(s"Room $id is not found")
            }
          } yield response
      }
  }
}