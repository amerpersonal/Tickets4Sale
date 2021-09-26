package tickets4sale.repository


import org.joda.time.LocalDate
import tickets4sale.models.Show

import scala.concurrent.Future

trait TicketOrderRepository {

  /**
    * Returns a tuple containing of (number of tickets ordered on a specific date for a specific performance, total number of tickets ordered for performance)
    * @param show
    * @param performanceDate - date for which we want to reserve a ticket
    * @param queryDate - date when tickets are ordered
    * @return
    */
  def getOrderedTicketsCount(show: Show, performanceDate: LocalDate, queryDate: LocalDate): Future[(Int, Int)]

  /**
    * Orders a ticket for show on a specific performance date. If there are no more tickets left, it will return failed Future
    * @param show
    * @param performanceDate
    * @return tuple containing of (number of tickets ordered on a current day for a specific performance, total number of tickets ordered for performance)
    */
  def orderTicket(show: Show, performanceDate: LocalDate): Future[(Int, Int)]
}