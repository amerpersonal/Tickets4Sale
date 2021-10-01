package tickets4sale

import org.joda.time.LocalDate
import spray.json._
import tickets4sale.config.Config
import tickets4sale.database.dsl.DatabaseOps
import tickets4sale.models.Show
import tickets4sale.repository.TicketOrderEmptyRepository
import tickets4sale.serializers.PerformanceInventorySerializer._
import tickets4sale.services.TicketOrderServiceFactory
import tickets4sale.utils.DateUtils
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.io.{BufferedSource, Source, StdIn}
import scala.util.Try

object Cli extends Config with DatabaseOps {

  def readLine(): Future[String] = Future(StdIn.readLine())

  def printText(msg: String): Future[Unit] = Future.successful(print(msg))

  def printLine(msg: String): Future[Unit] = Future.successful(println(msg))

  def readInput[T](enterMsg: String, errorMsg: String, f: String => T): Future[T] = {
    for {
      _ <- printText(enterMsg)
      enteredValue <- readLine()
      mapped = Try(f(enteredValue))
      r <- mapped.map(Future.successful).getOrElse(printLine(errorMsg).flatMap(_ => readInput(enterMsg, errorMsg, f)))

    } yield r
  }

  def main(args: Array[String]): Unit = {
    val registry = new TicketOrderServiceFactory with TicketOrderEmptyRepository {
      override val ticketOrderService: TicketOrderService = new TicketOrderService()
    }

    def loop(): Future[Unit] = {
      for {
        file <- readInput[BufferedSource]("Enter CSV file path: ", "You must specify valid CSV file", Source.fromFile)
        qd <- readInput[LocalDate]("Enter query date: ", s"You must specify valid query date in format: YYYY-dd-mm", DateUtils.parseDate)
        pd <- readInput[LocalDate]("Enter performance date: ", s"You must specify valid performance date in format: YYYY-dd-mm", DateUtils.parseDate)

        shows = Show.readAllValidFromCsv(file.getLines)

        inventory <- registry.ticketOrderService.totalInventoryForShows(shows, qd, pd)
        _ <- printLine(s"INVENTORY for query date ${qd.toString()} and performance date ${pd.toString}: ")
        _ <- printLine(inventory.toJson.prettyPrint)

        r <- loop()

      } yield r
    }

    Await.ready(loop(), Duration.Inf)
  }
}