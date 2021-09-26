package tickets4sale.api

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import tickets4sale.behaviors.Inventory
import tickets4sale.behaviors.Inventory.{CalculatePerformanceInventory, FullPerformanceInventory, InventoryMessage}
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.ExceptionHandler
import spray.json.DefaultJsonProtocol._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

trait Api extends Validators {
  import tickets4sale.serializers.FullPerformanceInventorySerializer._

  val apiPrefix = "api"
  val apiVersion = "v1"

//  implicit val timeout: Timeout
//  implicit val actorSystem: ActorSystem[InventoryMessage]
//  implicit val ec: ExecutionContext
//
//  val inventoryActor: ActorRef[Inventory.InventoryMessage]
//
//  val responseActor: ActorRef[Inventory.InventoryMessage]

  val routes = pathPrefix(apiPrefix / apiVersion) {
    get {
      path("performance_inventory") {
        validateInventoryDates("query_date", "performance_date") { case (queryDate, performanceDate) =>

//          val response: Future[FullPerformanceInventory] = (inventoryActor.ask(CalculatePerformanceInventory(queryDate, performanceDate, _))).mapTo[FullPerformanceInventory]

          complete("success")
        }
      }
    }
  }

  implicit def myExceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case NonFatal(e) =>
        println(s"Exception $e at\n${e.getMessage}")
        e.printStackTrace()
        complete(HttpResponse(StatusCodes.InternalServerError, entity = "Internal Server Error"))

    }

}
