package server

import cats.effect.implicits._
import cats.syntax.all._
import cats.effect.std.Queue
import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Ref, Resource, ResourceApp}
import com.comcast.ip4s.IpLiteralSyntax
import io.circe.syntax.EncoderOps
import io.circe._
import io.circe.generic.auto.exportEncoder
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.CORS
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import server.ServerGameState._
import org.http4s.circe._

import java.util.UUID
import scala.concurrent.duration.DurationInt

object ServerWebSockets extends ResourceApp.Forever {
  type PlayerId    = String
  type BoardCanvas = Map[(Int, Int), Int]

  // List of possible room names
  private val roomNames = List(
    "Pacific Ocean", "Atlantic Ocean", "Indian Ocean", "Southern Ocean", "Arctic Ocean",
    "Mediterranean Sea", "Caribbean Sea", "Red Sea", "Bering Sea", "South China Sea",
    "East China Sea", "Sulu Sea", "Caspian Sea", "North Sea", "Sea of Okhotsk",
    "Weddell Sea", "Coral Sea", "Philippine Sea", "Java Sea", "Adriatic Sea",
    "Black Sea", "Amazon River", "Nile River", "Yangtze River", "Mississippi River",
    "Ganges River", "Danube River", "Seine River", "Mekong River", "Congo River",
    "Paraná River", "Volga River", "São Francisco River", "Mackenzie River", "Thames River",
    "Lena River", "Indus River", "Zambezi River", "Limpopo River", "Colorado River",
    "Euphrates River", "Tigris River", "Murray River", "Oder River", "Hudson Bay",
    "Gulf of Mexico", "Baltic Sea", "Bay of Bengal", "Aegean Sea", "Gulf of Aden",
    "Chesapeake Bay", "Great Barrier Reef", "Sea of Japan", "Tasman Sea", "Bay of Biscay",
    "Labrador Sea", "Marmara Sea", "Sicilian Sea", "Lake Victoria", "Lake Baikal",
    "Great Lakes", "Lake Superior", "Lake Michigan", "Lake Huron", "Lake Ontario",
    "Lake Erie", "Lake Titicaca", "Lake Tanganyika", "Rio de la Plata", "Amur River",
    "Godavari River", "Mekong River", "Sao Francisco River", "Elbe River", "Rhine River",
    "Rio Grande", "Loire River", "Po River", "Volga River"
  )

  // Represents a game room with the game state and WebSocket connections of players
  final case class GameRoom(
                             state: ServerGameState,
                             webSockets: Map[PlayerId, Queue[IO, WebSocketFrame.Text]],
                             roomName: String
                           ) {
    // Handles received commands and updates the game state
    def handleCommand(c: ServerCommand): (GameRoom, Either[ServerGameState.BattleshipError, ServerGameState]) = {
      val newStateOrError =
        for {
          updatedState <- c match {
            case ServerCommand.Join(playerId)                    => state.join(playerId)
            case ServerCommand.PlaceShips(playerId, placements)  => state.placeShips(playerId, placements)
            case ServerCommand.AttackShips(playerId, coordinate) => state.attackShips(playerId, coordinate)
          }
        } yield copy(state = updatedState)

      (newStateOrError.getOrElse(this), newStateOrError.map(_.state))
    }
  }

  private val allowedOriginEnv = sys.env.getOrElse("ALLOWED_ORIGIN", "localhost")
  private val corsService = CORS.policy.withAllowOriginHost(origin => origin.host.value == allowedOriginEnv).withAllowCredentials(true)

  // Main method that starts the server
  override def run(args: List[String]): Resource[IO, Unit] = {
    for {
      // Initializes a map of game rooms
      gameRooms <- Ref.of[IO, Map[UUID, Ref[IO, GameRoom]]](Map.empty).toResource
      // Configures and starts the WebSocket server
      _         <- EmberServerBuilder
        .default[IO]
        .withHost(host"0.0.0.0")
        .withPort(port"8000")
        .withHttpWebSocketApp(wsb => corsService(QueueRoutes(gameRooms, wsb).orNotFound))
        .build
    } yield ()
  }

  // Defines HTTP routes for creating and joining games
  private object QueueRoutes {
    def apply(
               gameRooms: Ref[IO, Map[UUID, Ref[IO, GameRoom]]],
               webSocketBuilder: WebSocketBuilder2[IO]
             ): HttpRoutes[IO] =
      HttpRoutes.of[IO] {
        // Route to create a new game
        case POST -> Root / "createGame" =>
          for {
            id       <- IO.randomUUID
            // Get the list of already used room names
            usedNames <- gameRooms.get.flatMap { roomsMap =>
              val namesInUse = roomsMap.values.toList.map(_.get.unsafeRunSync().roomName).toSet
              IO.pure(namesInUse)
            }
            // Filter the available room names
            availableNames = roomNames.filterNot(usedNames.contains)
            roomName = availableNames(scala.util.Random.nextInt(availableNames.size))
            roomRef  <- Ref.of[IO, GameRoom](
              GameRoom(
                state = AwaitingPlayersServerPhase(Set.empty),
                webSockets = Map.empty,
                roomName = roomName
              )
            )
            _        <- gameRooms.update(_.updated(id, roomRef))
            response <- Created(id.toString)
          } yield response

        // Route to join an existing game
        case GET -> Root / "join" / roomId / playerId =>
          // Sends the game state to all players
          def sendStateToAllPlayers(
                                     newState: ServerGameState,
                                     stateRef: Ref[IO, GameRoom]
                                   ) = {
            val messagesToPlayers = newState.playerIds.toList.map { playerId =>
              playerId -> GameStateResponse.forPlayer(playerId, newState)
            }

            for {
              gameRoom <- stateRef.get
              _        <- messagesToPlayers.traverse_ { case (playerId, message) =>
                gameRoom.webSockets.get(playerId).traverse_ { playerWebSocket =>
                  val wsFrame = WebSocketFrame.Text(message.asJson.noSpaces)
                  playerWebSocket.offer(wsFrame)
                }
              }
            } yield ()
          }

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
                      for {
                        response <- webSocketBuilder.build(
                          send = fs2.Stream
                            .awakeEvery[IO](10.seconds)
                            .map(_ => WebSocketFrame.Ping())
                            .merge(fs2.Stream.fromQueueUnterminated(queue)),
                          receive = _.foreach {
                            case text: WebSocketFrame.Text =>
                              jawn.decode[ServerRequest](text.str) match {
                                case Left(decodingError) =>
                                  queue.offer(
                                    WebSocketFrame.Text(decodingError.toString)
                                  )
                                case Right(request)      =>
                                  // Match the request type and create the corresponding command
                                  val command = request match {
                                    case ServerRequest.PlaceShips(
                                    placements
                                    ) =>
                                      ServerCommand.PlaceShips(
                                        playerId,
                                        placements
                                      )
                                    case ServerRequest.AttackShips(coordinate) =>
                                      ServerCommand.AttackShips(
                                        playerId,
                                        coordinate
                                      )
                                  }

                                  for {
                                    // Handle the command and update the game state
                                    newStateOrError <- stateRef.modify {
                                      _.handleCommand(
                                        command
                                      )
                                    }
                                    // Send the updated state to all players
                                    _               <- newStateOrError match {
                                      case Left(e)         =>
                                        queue.offer(
                                          WebSocketFrame.Text(e.toString)
                                        )
                                      case Right(newState) =>
                                        sendStateToAllPlayers(
                                          newState,
                                          stateRef
                                        )
                                    }
                                  } yield ()
                              }

                            case _ => IO.unit
                          }
                        )
                        // Send the updated state to all players after joining
                        _        <- sendStateToAllPlayers(newState, stateRef)
                      } yield response
                  }
                } yield response

              case None => NotFound(s"Room $id is not found")
            }
          } yield response

        case GET -> Root / "rooms" =>
          for {
            roomsList <- gameRooms.get.map(_.toList)
            gameRoomInfos <- roomsList.traverse { case (id, roomRef) =>
              roomRef.get.map { room =>
                val hasEnded = room.state match {
                  case _: ServerGameState.WinServerPhase => true
                  case _                                 => false
                }

                val sunkShips: Map[PlayerId, List[String]] = room.state match {
                  case attack: ServerGameState.AttackServerPhase =>
                    List(attack.player1, attack.player2).map { player =>
                      val (threeCellShips, others) = player.ships.partition { coords =>
                        coords.forall(c => player.board.get(c).contains(Cell.HitShip)) && coords.size == 3
                      }

                      val threeCellNames: List[String] = threeCellShips.zipWithIndex.map {
                        case (_, 0) => "Cruiser"
                        case (_, 1) => "Submarine"
                        case _      => "Unknown 3-cell Ship"
                      }

                      val otherNames: List[String] = others.collect {
                        case coords if coords.forall(c => player.board.get(c).contains(Cell.HitShip)) =>
                          coords.size match {
                            case 5 => "Carrier"
                            case 4 => "Battleship"
                            case 2 => "Destroyer"
                            case 1 => "Patrol Boat"
                            case n => s"Unknown $n-cell Ship"
                          }
                      }

                      player.playerId -> (threeCellNames ++ otherNames)
                    }.toMap

                  case _ => Map.empty
                }

                GameRoomResponse(
                  id,
                  room.webSockets.size,
                  room.state.playerIds.toList,
                  room.roomName,
                  sunkShips,
                  hasEnded
                )
              }
            }

            response <- Ok(gameRoomInfos.asJson)
          } yield response
      }
  }
}
