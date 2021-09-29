package tickets4sale.models

import org.joda.time.{Days, LocalDate}
import org.joda.time.format.ISODateTimeFormat
import tickets4sale.models.Genres.Genre
import tickets4sale.repository.{ShowCSVRepository, TicketOrderEmptyRepository}
import tickets4sale.services.TicketOrderService
import tickets4sale.utils.DateUtils

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

case class Show(title: String, openingDay: LocalDate, genre: Genre) extends TicketOrderService with TicketOrderEmptyRepository with ShowCSVRepository {
  def inventory(queryDate: LocalDate, performanceDate: LocalDate)(implicit ec: ExecutionContext): Future[Option[PerformanceInventory]] = inventory(this, queryDate, performanceDate)
}

object Show {

  def readFromLine(line: String): Try[Show] = Try {
    val properties = line.split("\\,").map(_.trim)

    val openingDate = LocalDate.parse(properties(properties.size - 2), ISODateTimeFormat.date())
    val genre = Genres.fromName(properties(properties.size - 1)).get
    val name = properties.take(properties.size - 2).mkString(",")

    Show(name, openingDate, genre)

  }

  def readAllFromCsv(csvLines: Iterator[String]): (Seq[Show], Seq[Failure[Exception]]) = {
    val res = csvLines.toList.map(Show.readFromLine).partition(_.isSuccess)

    (res._1.map(_.get), res._2.map(_.asInstanceOf[Failure[Exception]]))
  }
}
