package tickets4sale.repository

import org.joda.time.LocalDate
import tickets4sale.models.Show
import scala.concurrent.Future

trait TicketOrderEmptyRepository extends TicketOrderRepository {
  def getReservedTickets(show: Show, queryDate: LocalDate, performanceDate: LocalDate): Future[(Int, Int)] = Future.successful((0, 0))

  def reserveTicket(show: Show, queryDate: LocalDate, performanceDate: LocalDate): Future[Int] = Future.successful(0)

  def getReservedTicketsForDay(show: Show, queryDate: LocalDate, performanceDate: LocalDate): Future[Int] = Future.successful(0)

  def getReservedTicketsBulk(queryDate: LocalDate, performanceDay: LocalDate): Future[Map[String, (Int, Int)]] = Future.successful(Map.empty)
}