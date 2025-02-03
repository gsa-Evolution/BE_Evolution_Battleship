package server

import cats.syntax.all._
import scala.annotation.tailrec

final case class Placement(
                            shipType: ShipType,
                            orientation: Orientation,
                            startCoordinate: Coordinate
                          )

sealed abstract class ShipType(val length: Int)

object ShipType {
  case object Cruiser extends ShipType(1)
  case object Destroyer extends ShipType(2)
  case object Submarine extends ShipType(3)
  case object Battleship extends ShipType(4)
  case object Carrier extends ShipType(5)
}

sealed trait Orientation

object Orientation {
  case object Vertical   extends Orientation
  case object Horizontal extends Orientation
}

final case class Coordinate(col: Int, row: Int) {

  def next(orientation: Orientation): Option[Coordinate] = {
    orientation match {
      case Orientation.Vertical   =>
        if (row == 0) None else Some(copy(row = row - 1))
      case Orientation.Horizontal =>
        if (col == 9) None else Some(copy(col = col + 1))
    }
  }

  def take(orientation: Orientation, amount: Int): Set[Coordinate] = {

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

object Main extends App {

  println(
    validatePlacement(
      List(
        Placement(ShipType.Cruiser, Orientation.Vertical, Coordinate(0, 6)),
        Placement(ShipType.Destroyer, Orientation.Vertical, Coordinate(2, 5)),
        Placement(ShipType.Submarine, Orientation.Horizontal, Coordinate(3, 4)),
        Placement(ShipType.Battleship, Orientation.Horizontal, Coordinate(4, 3)),
        Placement(ShipType.Carrier, Orientation.Horizontal, Coordinate(4, 2))
      )
    )
  )

  def validatePlacement(placements: List[Placement]): Either[String, Set[Coordinate]] = {
    val expectedAmountOfShipsPerType = Map(ShipType.Cruiser -> 1, ShipType.Destroyer -> 1, ShipType.Submarine -> 1, ShipType.Battleship -> 1, ShipType.Carrier -> 1)

    // Validate amount of ship types
    val actualAmountOfShipsPerType = placements.map { placement =>
      Map(placement.shipType -> 1)
    }.combineAll

    if (actualAmountOfShipsPerType != expectedAmountOfShipsPerType) {
      Left("Wrong amount of ship types")
    } else {

      placements.foldLeftM(Set.empty[Coordinate]) { (acc, placement) =>
        val shipCoordinates = placement.startCoordinate.take(placement.orientation, placement.shipType.length)

        if (shipCoordinates.size != placement.shipType.length) {
          Left(s"Invalid placement: $placement - ship out of bounds")
        } else if (acc.intersect(shipCoordinates).nonEmpty) {
          Left(s"Invalid placement: $placement - ships are intersecting")
        } else {
          Right(acc ++ shipCoordinates)
        }
      }
    }
  }

  //def validateAttack()
}