package tickets4sale.repository

import org.joda.time.LocalDate
import tickets4sale.models.Show

import scala.concurrent.Future

trait TicketOrderEmptyRepository extends TicketOrderRepository {
  def getOrderedTicketsCount(show: Show, performanceDate: LocalDate, queryDate: LocalDate): Future[(Int, Int)] = Future.successful((0, 0))

  def orderTicket(show: Show, performanceDate: LocalDate): Future[(Int, Int)] = Future.successful((0, 0))
}