package server

import io.circe.{Encoder, Decoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import scala.annotation.tailrec

final case class Coordinate(row: Int, column: Int) {
  def next(orientation: Orientation): Option[Coordinate] = {
    orientation match {
      case Orientation.Vertical   =>
        if (row == 0) None else Some(copy(row = row - 1))
      case Orientation.Horizontal =>
        if (column == 9) None else Some(copy(column = column + 1))
    }
  }

  def take(
      orientation: Orientation,
      amount: Int
  ): Set[Coordinate] = {
    @tailrec
    def loop(current: Coordinate, acc: Set[Coordinate]): Set[Coordinate] = {
      if (acc.size == amount) acc
      else
        current.next(orientation) match {
          case Some(nextCoordinate) => loop(nextCoordinate, acc + nextCoordinate)
          case None                 => acc
        }
    }
    loop(current = this, acc = Set(this))
  }

}

object Coordinate {

  implicit val decoder: Decoder[Coordinate] =
    deriveDecoder[Coordinate]
      .ensure { coordinate =>
        val validRange  = 0 to 9
        val rowError    = if (validRange.contains(coordinate.row)) List.empty else List("Row out of bounds")
        val columnError = if (validRange.contains(coordinate.column)) List.empty else List("Column out of bounds")

        rowError ::: columnError
      }

  implicit val encoder: Encoder[Coordinate] = deriveEncoder
}
