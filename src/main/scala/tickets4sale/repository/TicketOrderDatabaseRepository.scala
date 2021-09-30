package tickets4sale.repository

import org.joda.time.LocalDate
import slick.jdbc.PostgresProfile
import tickets4sale.database.DatabaseConnection
import tickets4sale.database.dsl.DatabaseOps
import tickets4sale.models.Show

import scala.concurrent.Future

trait TicketOrderDatabaseRepository extends TicketOrderRepository with DatabaseOps {
  def getReservedTickets(show: Show, queryDate: LocalDate, performanceDate: LocalDate): Future[(Int, Int)] =
    super[DatabaseOps].getReservedTickets(show.title, queryDate, performanceDate)

  def reserveTicket(show: Show, queryDate: LocalDate, performanceDate: LocalDate): Future[Int] = {
    reserveTicket(show.title, queryDate, performanceDate)
  }

  override def getReservedTicketsForDay(show: Show, queryDate: LocalDate, performanceDate: LocalDate): Future[Int] =
    super[DatabaseOps].getReservedTicketsForDay(show.title, queryDate, performanceDate)

}
