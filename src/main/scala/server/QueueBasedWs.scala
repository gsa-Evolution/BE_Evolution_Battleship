//import cats.effect.{IO, Resource, ResourceApp}
//import cats.effect.std.Queue
//import com.comcast.ip4s.IpLiteralSyntax
//import local.model.ActivePlayer
//import io.circe.{Json, jawn}
//import io.circe.syntax.EncoderOps
//import org.http4s.HttpRoutes
//import org.http4s.dsl.io._
//import org.http4s.ember.server.EmberServerBuilder
//import org.http4s.server.websocket.WebSocketBuilder2
//import org.http4s.websocket.WebSocketFrame
//
//import java.util.UUID
//
//object battleshipWS extends ResourceApp.Forever {
//
//  final case class GameRoom(
//      state: TicTacToe,
//      webSockets: Map[ActivePlayer, Queue[IO, WebSocketFrame.Text]]
//  ) {
//    def handleCommand(c: Command): (GameRoom, Either[TicTacToe.TicTacToeError, TicTacToe]) = {
//      val newStateOrError =
//        for {
//          updatedState <- c match {
//                            case Command.Join(playerId)                 => state.join(playerId)
//                            case Command.MakeMove(playerId, coordinate) => state.makeMove(playerId, coordinate)
//                          }
//        } yield copy(state = updatedState)
//
//      (newStateOrError.getOrElse(this), newStateOrError.map(_.state))
//    }
//  }
//
//  def run(args: List[String]): Resource[IO, Unit] =
//
//    for {
//      gameRooms <- Ref.of[IO, Map[UUID, Ref[IO, GameRoom]]](Map.empty).toResource
//      _         <- EmberServerBuilder
//                     .default[IO]
//                     .withHost(host"localhost")
//                     .withPort(port"8000")
//                     .withHttpWebSocketApp(QueueRoutes(gameRooms, _).orNotFound)
//                     .build
//    } yield ()
//}
//
//private object QueueRoutes {
//  def apply(
//      gameRooms: Ref[IO, Map[UUID, Ref[IO, GameRoom]]],
//      webSocketBuilder: WebSocketBuilder2[IO]
//  ): HttpRoutes[IO] =
//
//    HttpRoutes.of[IO] {
//      case POST -> Root / "createGame" =>
//        for {
//          id       <- IO.randomUUID
//          roomRef  <- Ref.of[IO, GameRoom](
//                        GameRoom(
//                          state = TicTacToe.AwaitingPlayers(Set.empty),
//                          webSockets = Map.empty
//                        )
//                      )
//          _        <- gameRooms.update(_.updated(id, roomRef))
//          response <- Created(id.toString)
//        } yield response
//
//      case GET -> Root / "join" / roomId / playerId =>
//        for {
//          id            <- IO(UUID.fromString(roomId))
//          maybeStateRef <- gameRooms.get.map(_.get(id))
//          response      <- maybeStateRef match {
//                             case Some(stateRef) =>
//                               for {
//                                 queue    <- Queue.unbounded[IO, WebSocketFrame.Text]
//                                 result   <- stateRef.modify { _.handleCommand(Command.Join(playerId)) }
//                                 _        <- if (result.isRight) stateRef.update { room =>
//                                               room.copy(webSockets = room.webSockets.updated(playerId, queue))
//                                             }
//                                             else IO.unit
//                                 response <- result match {
//                                               case Left(error)     => BadRequest(s"Failed to join room $id: $error")
//                                               case Right(newState) =>
//                                                 val newStateMsg = WebSocketFrame.Text(newState.asJson.noSpaces)
//                                                 for {
//                                                   response <- webSocketBuilder.build(
//                                                                 send = fs2.Stream.fromQueueUnterminated(queue),
//                                                                 receive = _.foreach {
//                                                                   case text: WebSocketFrame.Text =>
//                                                                     for {
//                                                                       request         <-
//                                                                         IO.fromEither(jawn.decode[Request](text.str))
//                                                                       command          = request match {
//                                                                                            case Request.MakeMove(coordinate) =>
//                                                                                              Command.MakeMove(playerId, coordinate)
//                                                                                          }
//                                                                       newStateOrError <- stateRef.modify {
//                                                                                            _.handleCommand(command)
//                                                                                          }
//
//                                                                       /// newState
//                                                                       ///
//
//                                                                       _ <- newStateOrError match {
//                                                                              case Left(e) =>
//                                                                                queue.offer(WebSocketFrame.Text(e.toString))
//
//                                                                              case Right(newState) =>
//                                                                                //
//
//                                                                                def sendToPlayer(
//                                                                                    playerId: PlayerId,
//                                                                                    msg: Json
//                                                                                ) =
//                                                                                  for {
//                                                                                    room             <- stateRef.get
//                                                                                    maybePlayersQueue =
//                                                                                      room.webSockets.get(playerId)
//                                                                                    _                <-
//                                                                                      maybePlayersQueue.traverse_ { q =>
//                                                                                        q.offer(
//                                                                                          WebSocketFrame.Text(msg.noSpaces)
//                                                                                        )
//                                                                                      }
//                                                                                  } yield ()
//
//                                                                                stateRef.get
//                                                                                  .map(room => room.webSockets.values.toList)
//                                                                                  .flatMap(webSockets =>
//                                                                                    webSockets.traverse(
//                                                                                      _.offer(
//                                                                                        WebSocketFrame
//                                                                                          .Text(newState.asJson.noSpaces)
//                                                                                      )
//                                                                                    )
//                                                                                  )
//                                                                            }
//                                                                     } yield ()
//
//                                                                   case _ => IO.unit
//                                                                 }
//                                                               )
//                                                   _        <- stateRef.get
//                                                                 .map(_.webSockets.values.toList)
//                                                                 .flatMap(_.traverse(_.offer(newStateMsg)))
//                                                 } yield response
//                                             }
//                               } yield response
//
//                             case None => NotFound(s"Room $id is not found")
//                           }
//        } yield response
//    }
//}
