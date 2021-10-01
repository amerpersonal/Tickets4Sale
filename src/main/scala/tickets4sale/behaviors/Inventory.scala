package tickets4sale.behaviors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import org.joda.time.LocalDate
import tickets4sale.behaviors.Inventory._
import tickets4sale.models.PerformanceInventory
import tickets4sale.services.TicketOrderServiceFactory
import scala.util.{Failure, Success}

object Inventory {
  sealed trait InventoryMessage

  final case class CalculatePerformanceInventory(queryDate: LocalDate, performanceDate: LocalDate, sender: ActorRef[FullPerformanceInventory]) extends InventoryMessage

  final case class ReserveTicket(name: String, queryDate: LocalDate, performanceDate: LocalDate, sender: ActorRef[ReservationStatus]) extends InventoryMessage

  sealed trait Response

  final case class FullPerformanceInventory(inventory: Map[String, Seq[PerformanceInventory]]) extends Response

  sealed trait ReservationStatus extends Response

  final case class ReservationSuccess(title: String, performanceDate: LocalDate, reservationDate: LocalDate, ticketsLeft: Int) extends ReservationStatus

  final case class ReservationFailure(err: Throwable) extends ReservationStatus
}

class Inventory { this: TicketOrderServiceFactory =>
  val ticketOrderService = new TicketOrderService()

  def apply(): Behavior[InventoryMessage] = {
    Behaviors.receive { (context, message) =>

      implicit val ec = context.system.executionContext

      message match {
        case CalculatePerformanceInventory(queryDate, performanceDate, sender) => {
          ticketOrderService.totalInventory(queryDate, performanceDate).onComplete {
            case Success(inventory) => sender ! FullPerformanceInventory(inventory)
            case Failure(ex) => context.log.error(s"Error on getting performance inventory: ${ex.getMessage}")
          }

          Behaviors.same
        }
        case ReserveTicket(title, queryDate, performanceDate, sender) => {
          ticketOrderService.reserve(title, queryDate, performanceDate).onComplete {
            case Success(ticketsLeft: Int) => sender ! ReservationSuccess(title, performanceDate, queryDate, ticketsLeft)
            case Failure(ex) => sender ! ReservationFailure(ex)
          }

          Behaviors.same
        }
      }
    }
  }
}