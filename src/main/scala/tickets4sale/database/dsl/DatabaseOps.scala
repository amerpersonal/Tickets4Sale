package tickets4sale.database.dsl


import org.joda.time.LocalDate
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import com.github.tototoshi.slick.PostgresJodaSupport._
import scala.concurrent.Future


trait DatabaseOps {

  case class Order(id: Long, title: String, reservationDate: LocalDate, performanceDate: LocalDate)

  class OrdersTable(tag: Tag) extends Table[Order](tag, Some("tickets"), "orders") {
    override def * = (id, title, reservationDate, performanceDate) <> (Order.tupled, Order.unapply) //.mapTo[Order]

    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def title: Rep[String] = column[String]("title")
    def reservationDate: Rep[LocalDate] = column[LocalDate]("reservation_date")
    def performanceDate: Rep[LocalDate] = column[LocalDate]("performance_date")
  }

  val ordersTable = TableQuery[OrdersTable]


  def reserveTicket(title: String, queryDate: LocalDate, performanceDate: LocalDate)(dbConn: PostgresProfile.backend.DatabaseDef): Future[Int] = {
    val query = ordersTable += Order(0L, title, queryDate, performanceDate)

    dbConn.run(query)
  }

  def getReservedTicketsForDay(title: String, queryDate: LocalDate, performanceDate: LocalDate)(dbConn: PostgresProfile.backend.DatabaseDef): Future[Int] = {
    val query = ordersTable.filter(order => order.title === title && order.performanceDate === performanceDate && order.reservationDate === queryDate).size

    dbConn.run[Int](query.result)
  }

  def getReservedTickets(title: String, queryDate: LocalDate, performanceDate: LocalDate)(dbConn: PostgresProfile.backend.DatabaseDef): Future[Int] = {
    val query = ordersTable.filter(order => order.title === title && order.performanceDate === performanceDate && order.reservationDate === queryDate).size

    dbConn.run[Int](query.result)
  }
}

