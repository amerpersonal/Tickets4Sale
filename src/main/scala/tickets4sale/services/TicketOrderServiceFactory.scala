package tickets4sale.services

import org.joda.time.LocalDate
import tickets4sale.config.Config
import tickets4sale.models.{Halls, PerformanceInventory, Show, TicketSaleStates}
import tickets4sale.repository.TicketOrderRepository
import scala.concurrent.Future

trait TicketOrderServiceFactory extends Config {
  this: TicketOrderRepository =>

  import scala.concurrent.ExecutionContext.Implicits.global

  val ticketOrderService: TicketOrderService

  class TicketOrderService {
    def getClearInventory(show: Show, queryDate: LocalDate, performanceDate: LocalDate): Option[PerformanceInventory] = {
      for {
        hall <- Halls.performanceHall(show, performanceDate)
      } yield PerformanceInventory(
        show,
        hall.ticketsLeft(queryDate, performanceDate),
        hall.ticketsAvailable(queryDate, performanceDate),
        TicketSaleStates.ticketState(queryDate, performanceDate)
      )
    }

    def inventory(show: Show, queryDate: LocalDate, performanceDate: LocalDate): Future[Option[PerformanceInventory]] = {
      for {
        clearInventory <- Future.successful(getClearInventory(show, queryDate, performanceDate))
        reservedTicketsCount <- if(clearInventory.nonEmpty) getReservedTickets(show, queryDate, performanceDate) else Future.successful((0, 0))

      } yield clearInventory.map { inv =>
        inv.copy(ticketsLeft = inv.ticketsLeft - reservedTicketsCount._2, ticketsAvailable = inv.ticketsAvailable - reservedTicketsCount._1)
      }
    }

    // this function could be implemented in a better way, using recursion to iterate over all inventories, instead of using group by
    def totalInventoryForShows(shows: Seq[Show], queryDate: LocalDate, performanceDate: LocalDate): Future[Map[String, Seq[PerformanceInventory]]] = {
      val clearInventories = shows.map(getClearInventory(_, queryDate, performanceDate))

      getReservedTicketsBulk(queryDate, performanceDate).map { reservations =>
        clearInventories.collect { case Some(inventory) =>
          inventory
        }.map { inv =>
          inv.copy(ticketsAvailable = inv.ticketsAvailable - reservations.get(inv.show.title).map(_._1).getOrElse(0), ticketsLeft = inv.ticketsLeft - reservations.get(inv.show.title).map(_._2).getOrElse(0))
        }.groupBy(_.show.genre.name)
      }
    }

    def totalInventory(queryDate: LocalDate, performanceDate: LocalDate): Future[Map[String, Seq[PerformanceInventory]]] = {
      totalInventoryForShows(Show.all, queryDate, performanceDate)
    }

    // we want to load shows from CSV file only on program start, to avoid executing costly IO operation on each API request
    def reserve(title: String, queryDate: LocalDate, performanceDate: LocalDate): Future[Int] = {
      for {
        show <- Show.all.find(_.title == title).map(Future.successful).getOrElse(Future.failed(new Throwable("Show not exists")))
        performanceInventoryOpt <- inventory(show, queryDate, performanceDate)

        performanceInventoryTransformed = performanceInventoryOpt.flatMap(i => if (i.status == TicketSaleStates.OpenForSale) Some(i) else None)

        peformanceInventory <- performanceInventoryTransformed.map(Future.successful(_)).getOrElse(Future.failed(new Throwable("Show not running")))

        result <- if (peformanceInventory.ticketsAvailable == 0) Future.failed(new Throwable("No tickets left for ordering on this day")) else reserveTicket(show, queryDate, performanceDate)
      } yield peformanceInventory.ticketsAvailable - result
    }
  }
}