package tickets4sale.models

import org.joda.time.LocalDate
import org.joda.time.format.ISODateTimeFormat
import tickets4sale.config.Config
import tickets4sale.models.Genres.Genre
import scala.io.Source
import scala.util.{Failure, Success, Try}

case class Show(title: String, openingDay: LocalDate, genre: Genre) extends Config {
  def isRunning(performanceDate: LocalDate) = performanceDate.isAfter(openingDay.minusDays(1)) && performanceDate.isBefore(openingDay.plusDays(showDuration + 1))
}

object Show extends Config {

  // we want to load shows from CSV file only on program start, to avoid executing costly IO operation on each API request
  val all = readAllFromCsv(Source.fromFile(csvPath).getLines())._1

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

  def readAllValidFromCsv(csvLines: Iterator[String]): Seq[Show] = {
    csvLines.toList.map(Show.readFromLine).collect { case Success(inv) => inv}
  }
}