package tickets4sale.behaviors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import org.joda.time.LocalDate
import tickets4sale.behaviors.Inventory._
import tickets4sale.database.DatabaseConnection
import tickets4sale.database.dsl.DatabaseOps
import tickets4sale.models.{PerformanceInventory, Show}
import tickets4sale.repository.{ShowCSVRepository, ShowRepository, TicketOrderDatabaseRepository, TicketOrderRepository}
import tickets4sale.services.{ShowsService, TicketOrderServiceFactory}

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source
import scala.util.{Failure, Success}

object Inventory {
  sealed trait InventoryMessage

  final case class CalculatePerformanceInventory(queryDate: LocalDate, performanceDate: LocalDate, sender: ActorRef[FullPerformanceInventory]) extends InventoryMessage

  final case class ReserveTicket(name: String, queryDate: LocalDate, performanceDate: LocalDate, sender: ActorRef[ReservationStatus]) extends InventoryMessage

  final case class FullPerformanceInventory(inventory: Map[String, Seq[PerformanceInventory]]) extends InventoryMessage

  final case class ReservationStatus(title: String, performanceDate: LocalDate, reservationDate: LocalDate, ticketsLeft: Int, err: Option[Throwable] = None)

}

class Inventory { this: TicketOrderServiceFactory =>
  val ticketOrderService = new TicketOrderService()

  def apply(): Behavior[InventoryMessage] = {
    Behaviors.receive { (context, message) =>

      implicit val ec = context.system.executionContext

      message match {
        case CalculatePerformanceInventory(queryDate, performanceDate, sender) => {

          val lines = Source.fromFile("shows.csv").getLines()
          val (shows, failures) = Show.readAllFromCsv(lines)

          totalInventory(queryDate, performanceDate, shows).onComplete {
            case Success(inventory) => {
              sender ! FullPerformanceInventory(inventory)
            }
            case Failure(ex) => {
              context.log.error(s"Error on getting performance inventory: ${ex.getMessage}")
            }
          }

          Behaviors.same


        }
        case ReserveTicket(title, queryDate, performanceDate, sender) => {
          ticketOrderService.reserve(title, queryDate, performanceDate).onComplete {
            case Success(ticketsLeft: Int) => {
              sender ! ReservationStatus(title, performanceDate, queryDate, ticketsLeft)
            }
            case Failure(ex) => {
              println("failed")
              throw ex
            }
          }

          Behaviors.same


        }
      }
    }

  }

  def totalInventory(queryDate: LocalDate, performanceDate: LocalDate, shows: Seq[Show])(implicit ec: ExecutionContext): Future[Map[String, Seq[PerformanceInventory]]] = {
    val showsByGenre = shows.groupBy(_.genre).map { case (genre, shows) =>
      (genre.name, shows)
    }

    showsByGenre.map { case (genre, shows) =>
      shows.map { s =>
        ticketOrderService.inventory(s, queryDate, performanceDate)
      }
    }

    val calculations = shows.map(ticketOrderService.inventory(_, queryDate, performanceDate))

    Future.sequence(calculations).map { inventories =>
      inventories.collect { case Some(inventory) => inventory }.groupBy(_.show.genre.name)
    }

  }


}
