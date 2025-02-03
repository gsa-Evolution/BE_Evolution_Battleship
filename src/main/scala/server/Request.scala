//import TicTacToe.Coordinate
//import io.circe.Decoder
//import io.circe.generic.extras.Configuration
//import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
//import io.circe.generic.semiauto.deriveDecoder
//
//sealed trait Request
//
//object Request {
//  final case class MakeMove(coordinate: Coordinate) extends Request
//  object MakeMove {
//    implicit val decoder: Decoder[MakeMove] = deriveDecoder
//  }
//
//  implicit val decoder: Decoder[Request] = {
//    implicit val configuration: Configuration = Configuration.default.withDiscriminator("type")
//    deriveConfiguredDecoder
//  }
//}
