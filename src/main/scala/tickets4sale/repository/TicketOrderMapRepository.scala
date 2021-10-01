package tickets4sale.repository

import org.joda.time.LocalDate
import tickets4sale.models.{Show, Ticket}
import scala.collection.mutable
import scala.concurrent.Future

trait TicketOrderMapRepository extends TicketOrderRepository {
  val reservations: mutable.Map[Show, List[Ticket]] = mutable.Map.empty[Show, List[Ticket]]

  def getReservedTickets(show: Show, queryDate: LocalDate, performanceDate: LocalDate): Future[(Int, Int)] = {
    val ticketsReserved = reservations.getOrElse(show, Seq.empty).filter(_.performanceDate == performanceDate)
    val ticketsReservedOnDay = ticketsReserved.filter(_.reservcationDate == queryDate)
    Future.successful((ticketsReservedOnDay.size, ticketsReserved.size))
  }

  def reserveTicket(show: Show, queryDate: LocalDate, performanceDate: LocalDate): Future[Int] = {
    val ticket = Ticket(show, queryDate, performanceDate)
    reservations.update(show, ticket :: reservations.getOrElse(show, List.empty))

    Future.successful(1)
  }

  def getReservedTicketsForDay(show: Show, queryDate: LocalDate, performanceDate: LocalDate): Future[Int] = {
    val ticketsReserved = reservations.getOrElse(show, Seq.empty).filter(_.performanceDate == performanceDate)
    val ticketsReservedOnDay = ticketsReserved.filter(_.reservcationDate == queryDate)

    Future.successful(ticketsReservedOnDay.size)
  }

  def getReservedTicketsBulk(queryDate: LocalDate, performanceDate: LocalDate): Future[Map[String, (Int, Int)]] = {
    val reservedTickets = reservations.map { case (show, tickets) =>
      val ticketsReserved = tickets.filter(_.performanceDate == performanceDate)
      val ticketsReservedOnDay = ticketsReserved.filter(_.reservcationDate == queryDate)

      show.title -> (ticketsReservedOnDay.size, ticketsReserved.size)
    }.toMap

    Future.successful(reservedTickets)
  }

}
