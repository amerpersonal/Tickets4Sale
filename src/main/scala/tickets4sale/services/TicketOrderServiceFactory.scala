package tickets4sale.services

import org.joda.time.LocalDate
import tickets4sale.config.Config
import tickets4sale.models.{Halls, PerformanceInventory, Show, TicketSaleState}
import tickets4sale.repository.{ShowRepository, TicketOrderRepository}
import scala.concurrent.{ExecutionContext, Future}

trait TicketOrderServiceFactory extends Config {
  this: TicketOrderRepository with ShowRepository =>
  val ticketOrderService: TicketOrderService

  class TicketOrderService {
    def getClearInventory(show: Show, queryDate: LocalDate, performanceDate: LocalDate)(implicit ec: ExecutionContext): Option[PerformanceInventory] = {
      for {
        hall <- Halls.performanceHall(show, performanceDate)
      } yield PerformanceInventory(
        show,
        hall.ticketsLeft(queryDate, performanceDate),
        hall.ticketsAvailable(queryDate, performanceDate),
        TicketSaleState.ticketState(queryDate, performanceDate)
      )

    }

    def inventory(show: Show, queryDate: LocalDate, performanceDate: LocalDate)(implicit ec: ExecutionContext): Future[Option[PerformanceInventory]] = {
      for {
        clearInventory <- Future.successful(getClearInventory(show, queryDate, performanceDate))
        reservedTicketsCount <- if(clearInventory.nonEmpty) getReservedTickets(show, queryDate, performanceDate) else Future.successful((0, 0))

      } yield clearInventory.map { inv =>
        inv.copy(ticketsLeft = inv.ticketsLeft - reservedTicketsCount._2, ticketsAvailable = inv.ticketsAvailable - reservedTicketsCount._1)
      }
    }

    def totalInventory(queryDate: LocalDate, performanceDate: LocalDate)(implicit ec: ExecutionContext): Future[Map[String, Seq[PerformanceInventory]]] = {
      val calculations = Show.all.map(inventory(_, queryDate, performanceDate))

      Future.sequence(calculations).map { inventories =>
        inventories.collect { case Some(inventory) => inventory }.groupBy(_.show.genre.name)
      }
    }

    def isRunning(show: Show, performanceDate: LocalDate): Boolean = {
      performanceDate.isAfter(show.openingDay.minusDays(1)) && performanceDate.isBefore(show.openingDay.plusDays(showDuration))
    }

    def reserve(title: String, queryDate: LocalDate, performanceDate: LocalDate)(implicit ec: ExecutionContext): Future[Int] = {
      //      Show.all.find(_.title == title).map { show =>
      //
      //        Halls.performanceHall(show, performanceDate).map { hall =>
      //
      //          println(s"xxx hall: ${hall}")
      //
      //          getReservedTicketsForDay(title, queryDate, performanceDate).flatMap { reservedTickets =>
      //            println(s"xxx ta: ${hall.ticketsAvailable(queryDate, performanceDate)}, rt: ${reservedTickets}")
      //            val ticketsLeftForQueryDate = hall.ticketsAvailable(queryDate, performanceDate) - reservedTickets
      //
      //            if (ticketsLeftForQueryDate == 0) Future.failed(new Throwable("No tickets left for ordering on this day"))
      //            else {
      //              println("xxx order ticket")
      //              orderTicket(show, queryDate, performanceDate).map(ticketsLeftForQueryDate - _)
      //            }
      //          }
      //
      //        }.getOrElse {
      //          Future.failed(new Throwable("Show not running"))
      //        }
      //      }.getOrElse {
      //        Future.failed(new Throwable("Invalid show"))
      //      }

      for {
        show <- Show.all.find(_.title == title).map(Future.successful).getOrElse(Future.failed(new Throwable("Show not exists")))
        performanceInventory <- inventory(show, queryDate, performanceDate).map(_.get)

        result <- if (performanceInventory.ticketsAvailable == 0) Future.failed(new Throwable("No tickets left for ordering on this day")) else reserveTicket(show, queryDate, performanceDate)
      } yield performanceInventory.ticketsAvailable - result
    }
  }
}