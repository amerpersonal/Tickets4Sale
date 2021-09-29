package tickets4sale.repository

import org.joda.time.LocalDate
import slick.jdbc.PostgresProfile
import tickets4sale.database.dsl.DatabaseOps
import tickets4sale.models.Show

import scala.concurrent.Future

trait TicketOrderDatabaseRepository extends TicketOrderRepository with DatabaseOps {
  def getOrderedTicketsCount(show: Show, performanceDate: LocalDate, queryDate: LocalDate): Future[(Int, Int)] = Future.successful(0, 0)

  def orderTicket(show: Show, queryDate: LocalDate, performanceDate: LocalDate, dbConn: PostgresProfile.backend.DatabaseDef): Future[Int] = {
    reserveTicket(show.title, queryDate, performanceDate)(dbConn)
  }

  override def getReservedTicketsForDay(title: String, queryDate: LocalDate, performanceDate: LocalDate, dbConn: PostgresProfile.backend.DatabaseDef): Future[Int] =
    super[DatabaseOps].getReservedTicketsForDay(title, queryDate, performanceDate, dbConn)

}
