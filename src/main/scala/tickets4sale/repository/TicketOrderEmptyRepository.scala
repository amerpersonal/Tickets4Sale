package tickets4sale.repository

import org.joda.time.LocalDate
import slick.jdbc.PostgresProfile
import tickets4sale.models.Show

import scala.concurrent.Future

trait TicketOrderEmptyRepository extends TicketOrderRepository {
  def getReservedTickets(show: Show, queryDate: LocalDate, performanceDate: LocalDate): Future[(Int, Int)] = Future.successful((0, 0))

  def reserveTicket(show: Show, queryDate: LocalDate, performanceDate: LocalDate): Future[Int] = Future.successful(0)

  def getReservedTicketsForDay(show: Show, queryDate: LocalDate, performanceDate: LocalDate): Future[Int] = Future.successful(0)

}