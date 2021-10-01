package tickets4sale

//import java.time.LocalDate
//import java.time.format.DateTimeFormatter
//import java.time.temporal.{ChronoField, ChronoUnit, TemporalField}
//import java.util.Calendar


import org.joda.time.LocalDate
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter, ISODateTimeFormat}
import spray.json.{JsArray, JsObject, JsString, JsValue, RootJsonFormat}
import tickets4sale.models._
import tickets4sale.utils.DateUtils
import spray.json._
import tickets4sale.config.Config

import scala.io.{Source, StdIn}
import scala.util.{Failure, Success, Try}
import tickets4sale.serializers.PerformanceInventorySerializer._
import tickets4sale.serializers.ShowSerializer._
import slick.jdbc.PostgresProfile
import org.postgresql.PGConnection
import slick.jdbc.PostgresProfile.api._
import tickets4sale.database.dsl.DatabaseOps
import tickets4sale.repository.TicketOrderDatabaseRepository
import tickets4sale.services.TicketOrderServiceFactory

import scala.concurrent.ExecutionContext.Implicits.global

object Runner extends Config with DatabaseOps {
  import DateUtils._

  def readLine(): Option[String] = Some(StdIn.readLine())

  def printLine(msg: String): Option[Unit] = Some(println(msg))


  def main(args: Array[String]): Unit = {

    val qd = parseDate("2021-10-30")
    val pd = parseDate("2021-11-05")

    //    val shows = Seq(
    //      Show("Cats", parseDate("2018-06-01"), Genres.Musical),
    //      Show("Comedy of Errors", parseDate("2018-07-01"), Genres.Comedy),
    //      Show("Everyman", parseDate("2018-08-01"), Genres.Drama)
    //    )

    val lines = Source.fromFile("shows.csv").getLines()
    val (shows, failures) = Show.readAllFromCsv(lines)

    println(s"shows size ${shows.size}")

//    val ti = totalInventory(qd, pd, shows)
//
//    println(ti.toJson.toString())



//    reserveTicket("cats", LocalDate.now(), LocalDate.now().plusDays(10)).onComplete {
//      case Success(r) => println(s"xxx r: ${r}")
//      case Failure(ex: Throwable) => println(s"error: ${ex.getMessage}")
//    }

    val repo = new TicketOrderDatabaseRepository {}

    repo.getReservedTicketsBulk(DateUtils.parseDate("2021-11-25"), DateUtils.parseDate("2021-12-05")).onComplete {
      case Success(r) => println(s"xxx r: ${r}")
      case Failure(ex: Throwable) => println(s"error: ${ex.getMessage}")
    }

    Thread.sleep(5000)
  }

  def dbUrl() = {
    val sslOptions = if(dbUseSsl) s"&ssl=${dbUseSsl}&sslfactory=org.postgresql.ssl.NonValidatingFactory&sslmode=prefer" else ""

    val connectionString = s"jdbc:postgresql://${dbHost}:${dbPort}/${dbName}?ApplicationName=${dbAppName}${sslOptions}"

    connectionString
  }
}
