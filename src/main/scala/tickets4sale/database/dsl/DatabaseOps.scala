package tickets4sale.database.dsl


import org.joda.time.LocalDate
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import com.github.tototoshi.slick.PostgresJodaSupport._
import tickets4sale.database.DatabaseConnection

import scala.concurrent.Future


trait DatabaseOps {

  import scala.concurrent.ExecutionContext.Implicits.global

  case class Order(id: Long, title: String, reservationDate: LocalDate, performanceDate: LocalDate)

  class OrdersTable(tag: Tag) extends Table[Order](tag, Some("tickets"), "orders") {
    override def * = (id, title, reservationDate, performanceDate) <> (Order.tupled, Order.unapply) //.mapTo[Order]

    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def title: Rep[String] = column[String]("title")
    def reservationDate: Rep[LocalDate] = column[LocalDate]("reservation_date")
    def performanceDate: Rep[LocalDate] = column[LocalDate]("performance_date")
  }

  val ordersTable = TableQuery[OrdersTable]


  def reserveTicket(title: String, queryDate: LocalDate, performanceDate: LocalDate): Future[Int] = {
    val query = ordersTable += Order(0L, title, queryDate, performanceDate)

    DatabaseConnection.connection.run(query)
  }

  def getReservedTicketsForDay(title: String, queryDate: LocalDate, performanceDate: LocalDate): Future[Int] = {
    val query = ordersTable.filter(order => order.title === title && order.performanceDate === performanceDate && order.reservationDate === queryDate).size

    DatabaseConnection.connection.run[Int](query.result)
  }

  def getReservedTickets(title: String, queryDate: LocalDate, performanceDate: LocalDate): Future[(Int, Int)] = {
    val query =
      sql"""select * from tickets.get_reserved_tickets(
                           p_title := #${escapeString(title)},
                           p_query_date := '#${queryDate.toString}',
                           p_performance_date := '#${performanceDate.toString()}'
                          )""".as[(Int, Int)]

    DatabaseConnection.connection.run(query).map(_.head)
  }


  def getReservedTicketsBulkOnDay(queryDate: LocalDate, performanceDate: LocalDate): Future[Map[String, Int]] = {
    val query =
      sql"""select * from tickets.get_reserved_tickets_bulk_on_day(
                           p_query_date := '#${queryDate.toString}',
                           p_performance_date := '#${performanceDate.toString()}'
                          )""".as[(String, Int)]

    DatabaseConnection.connection.run(query).map(_.toMap)
  }

  def getReservedTicketsBulkTotal(performanceDate: LocalDate): Future[Map[String, Int]] = {
    val query =
      sql"""select * from tickets.get_reserved_tickets_bulk(
                           p_performance_date := '#${performanceDate.toString()}'
                          )""".as[(String, Int)]

    DatabaseConnection.connection.run(query).map(_.toMap)
  }

  private def escapeString(str: String): String = {
    "$$" + str + "$$"
  }
}

