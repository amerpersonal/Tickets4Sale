package tickets4sale.behaviors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import org.joda.time.LocalDate
import tickets4sale.models.{PerformanceInventory, Show}

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source
import scala.util.{Failure, Success}

object Inventory {
  sealed trait InventoryMessage

  final case class CalculatePerformanceInventory(queryDate: LocalDate, performanceDate: LocalDate, sender: ActorRef[FullPerformanceInventory]) extends InventoryMessage


  final case class FullPerformanceInventory(inventory: Map[String, Seq[PerformanceInventory]]) extends InventoryMessage

  def apply(): Behavior[CalculatePerformanceInventory] = {
    Behaviors.receive { case (context, message) =>
      implicit val ec = context.system.executionContext

      message match {
        case CalculatePerformanceInventory(queryDate, performanceDate, sender) => {

          val lines = Source.fromFile("shows.csv").getLines()
          val (shows, failures) = Show.readAllFromCsv(lines)

//          sender ! FullPerformanceInventory(totalInventory(queryDate, performanceDate, shows)) //s"${queryDate} - ${performanceDate}")

          totalInventory(queryDate, performanceDate, shows).onComplete {
            case Success(inventory) => sender ! FullPerformanceInventory(inventory)
            case Failure(ex) => context.log.error(s"Error on getting performance inventory: ${ex.getMessage}")
          }


          Behaviors.same
        }
      }
    }
  }

  def totalInventory(queryDate: LocalDate, performanceDate: LocalDate, shows: Seq[Show])(implicit ec: ExecutionContext): Future[Map[String, Seq[PerformanceInventory]]] = {
    val showsByGenre = shows.groupBy(_.genre).map { case (genre, shows) =>
      (genre.name, shows)
    }


    showsByGenre.map { case (genre, shows) =>
      shows.map { s =>
        s.inventory(queryDate, performanceDate)
      }
    }


    val calculations = shows.map(_.inventory(queryDate, performanceDate))

    Future.sequence(calculations).map { inventories =>
      inventories.collect { case Some(inventory) => inventory }.groupBy(_.show.genre.name)
    }

  }


}