package tickets4sale.services

import org.joda.time.{Days, LocalDate}
import tickets4sale.config.Config
import tickets4sale.models.{Halls, PerformanceInventory, Show, TicketSaleState}
import tickets4sale.repository.{ShowRepository, TicketOrderRepository}

import scala.concurrent.{ExecutionContext, Future}

trait TicketOrderService extends Config { this: TicketOrderRepository with ShowRepository =>
  def inventory(show: Show, queryDate: LocalDate, performanceDate: LocalDate)(implicit ec: ExecutionContext): Future[Option[PerformanceInventory]] = {

    if (isRunning(show, performanceDate)) {

      val remainingDaysUntilPerformance = Days.daysBetween(queryDate, performanceDate).getDays + 1

      val showRunningForDays = Days.daysBetween(show.openingDay, performanceDate).getDays + 1


      getOrderedTicketsCount(show, performanceDate, queryDate).map { case (numberOfOrderedTickets, numberOfOrderedTicketOnDay) =>

        val hall = Halls.performanceHall(showRunningForDays)
        val ticketsLeft = hall.map(_.ticketsLeft(remainingDaysUntilPerformance) - numberOfOrderedTicketOnDay).getOrElse(0)
        val ticketsAvailable = hall.map(_.ticketsAvailable(remainingDaysUntilPerformance) - numberOfOrderedTickets).getOrElse(0)

        val status = if (remainingDaysUntilPerformance > saleStartsBefore) TicketSaleState.SaleNotStarted
        else if (remainingDaysUntilPerformance < saleEndsBefore && remainingDaysUntilPerformance > saleEndsBefore) TicketSaleState.OpenForSale
        else if (remainingDaysUntilPerformance > 0 && remainingDaysUntilPerformance < saleEndsBefore) TicketSaleState.SoldOut
        else TicketSaleState.InThePast

        Some(PerformanceInventory(show, ticketsLeft, ticketsAvailable, status))

      }
    }
    else Future.successful(None)
  }

  def isRunning(show: Show, performanceDate: LocalDate): Boolean = {
    performanceDate.isAfter(show.openingDay.minusDays(1)) && performanceDate.isBefore(show.openingDay.plusDays(showDuration))
  }

  def reserve(title: String, queryDate: LocalDate, performanceDate: LocalDate): Future[Int] = {
    loadShows().find(title ==).map { show =>

      val runningForDays = Days.daysBetween(show.openingDay, queryDate).getDays + 1

      Halls.performanceHall(runningForDays).map { hall =>


        getReservedTicketsForDay(title, queryDate, performanceDate).flatMap { reservedTickets =>
          val ticketsLeftForToday = hall.ticketsAvailable(queryDate, performanceDate) - reservedTickets

          if (ticketsLeftForToday == 0) Future.failed(throw new Throwable("No tickets left for ordering on this day"))
          else reserve()
        }

      }.getOrElse {
        Future.failed(new Throwable("Show not running"))
      }
    }.getOrElse {
      Future.failed(new Throwable("Invalid show"))
    }

  }

}
