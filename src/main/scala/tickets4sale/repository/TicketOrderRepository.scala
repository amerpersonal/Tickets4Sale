package tickets4sale.repository


import org.joda.time.LocalDate
import slick.jdbc.PostgresProfile
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
  def getReservedTickets(show: Show, queryDate: LocalDate, performanceDate: LocalDate): Future[(Int, Int)]

  /**
    * Orders a ticket for show on a specific performance date. If there are no more tickets left, it will return failed Future
    * @param show
    * @param performanceDate
    * @return tuple containing of (number of tickets ordered on a current day for a specific performance, total number of tickets ordered for performance)
    */
  def reserveTicket(show: Show, queryDate: LocalDate, performanceDate: LocalDate): Future[Int]


  /**
    * Get a number of tickets reserved on a specific day for a specific performance
    * @param show
    * @param queryDate
    * @param performanceDate
    * @return
    */
  def getReservedTicketsForDay(show: Show, queryDate: LocalDate, performanceDate: LocalDate): Future[Int]


  /**
    * Get a number of reserved ticker per show for a specific performance date, both total and only those reserved on queryDate
    * @param queryDate
    * @param performanceDay
    * @return map, containing number of tickets reserved for each performance in format (number of tickets reserved on query date, total number of tickets reserved)
    */
  def getReservedTicketsBulk(queryDate: LocalDate, performanceDay: LocalDate): Future[Map[String, (Int, Int)]]

}
