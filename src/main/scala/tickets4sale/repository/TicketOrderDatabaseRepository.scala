package tickets4sale.repository

import org.joda.time.LocalDate
import tickets4sale.database.dsl.DatabaseOps
import tickets4sale.models.Show
import scala.concurrent.Future

trait TicketOrderDatabaseRepository extends TicketOrderRepository with DatabaseOps {
  import scala.concurrent.ExecutionContext.Implicits.global

  def getReservedTickets(show: Show, queryDate: LocalDate, performanceDate: LocalDate): Future[(Int, Int)] =
    getReservedTickets(show.title, queryDate, performanceDate)

  def reserveTicket(show: Show, queryDate: LocalDate, performanceDate: LocalDate): Future[Int] = {
    reserveTicket(show.title, queryDate, performanceDate)
  }

  def getReservedTicketsForDay(show: Show, queryDate: LocalDate, performanceDate: LocalDate): Future[Int] =
    getReservedTicketsForDay(show.title, queryDate, performanceDate)

  // 2 functions are deliberately called in parallel, to better utilise multithreading environment, instead of executing single large query for finding both total and on day reservations
  def getReservedTicketsBulk(queryDate: LocalDate, performanceDate: LocalDate): Future[Map[String, (Int, Int)]] = {
    Future.sequence(Seq(
      getReservedTicketsBulkOnDay(queryDate, performanceDate),
      getReservedTicketsBulkTotal(performanceDate)
    )).map { res =>
      val countOnDay = res(0)
      val countTotal = res(1)

      (countOnDay.keySet ++ countOnDay.keySet).map { key =>
        key -> (countOnDay.getOrElse(key, 0), countTotal.getOrElse(key, 0))
      }.toMap
    }
  }


}
