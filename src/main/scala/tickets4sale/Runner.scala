package tickets4sale

//import java.time.LocalDate
//import java.time.format.DateTimeFormatter
//import java.time.temporal.{ChronoField, ChronoUnit, TemporalField}
//import java.util.Calendar

import org.joda.time.format.{DateTimeFormat, DateTimeFormatter, ISODateTimeFormat}
import org.joda.time.{DateTime, Days, LocalDate, ReadablePeriod}
import spray.json.{JsArray, JsObject, JsString, JsValue, RootJsonFormat}
import tickets4sale.models._
import tickets4sale.utils.DateUtils
import spray.json._
import scala.io.{Source, StdIn}
import scala.util.Try
import tickets4sale.serializers.PerformanceInventorySerializer._
import tickets4sale.serializers.ShowSerializer._


object Runner {
  import DateUtils._

  def readLine(): Option[String] = Some(StdIn.readLine())

  def printLine(msg: String): Option[Unit] = Some(println(msg))


  def main(args: Array[String]): Unit = {


//    def totalInventory(queryDate: LocalDate, performanceDate: LocalDate, shows: Seq[Show]): Map[String, Seq[Either[PerformanceInventory, Show]]] = {
//      val showsByGenre = shows.groupBy(_.genre).map { case (genre, shows) =>
//        (genre.name, shows)
//      }
//
//
//      showsByGenre.mapValues { shows =>
//        shows.map { s =>
//          s.inventory(queryDate, performanceDate).map(Left(_)).getOrElse(Right(s))
//        }
//      }
//    }





    //    for {
    //      _ <- printLine("Performance date: ")
    //      pds <- readLine()
    //      pd <- Try(LocalDate.parse(pds, ISODateTimeFormat.basicDate())).toOption.orElse(throw Exception("Invalid date"); LocalDate.now())
    //    } yield println(Days.daysBetween(LocalDate.now(), pd))

    //    printLine("Performance date: ")

    //    val pds = StdIn.readLine()

    //    val pd = LocalDate.parse("2018-01-01", ISODateTimeFormat.date())
    //
    //    val lines = Source.fromFile("shows.csv").getLines()
    //    val (shows, failures) = Show.readAllFromCsv(lines)
    //
    //    println(failures.head.exception.getMessage)

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


  }

}
