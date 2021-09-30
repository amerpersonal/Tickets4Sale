package tickets4sale.api

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import org.joda.time.{DateTimeZone, LocalDate}
import tickets4sale.api.WebServerTyped.validateInventoryDates
import tickets4sale.behaviors.Inventory._
import tickets4sale.models.requests.ReserveTicketRequest
import spray.json._

import scala.concurrent.Future
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.model.StatusCodes
import akka.util.Timeout

import scala.util.{Failure, Success}

class Router(inventoryActor: ActorRef[InventoryMessage])(implicit system: ActorSystem[_], timeout: Timeout) extends Validators {
  import tickets4sale.serializers.FullPerformanceInventorySerializers._
  import tickets4sale.serializers.ReservationStatusSerializer._
  import tickets4sale.serializers.requests.ReserveTicketRequestSerializer._

  val routes = pathPrefix("api" / "v1") {
    get {
      path("performance_inventory") {
        validateInventoryDates("query_date", "performance_date") { case (queryDate, performanceDate) =>

          val response: Future[FullPerformanceInventory] = inventoryActor.ask(CalculatePerformanceInventory(queryDate, performanceDate, _))

          complete(response)
        }
      }
    } ~
      post {
        path("show" / "reserve_ticket") {
          entity(as[ReserveTicketRequest]) { request =>

            val response: Future[ReservationStatus] = inventoryActor.ask(ReserveTicket(request.title, request.queryDate.getOrElse(LocalDate.now(DateTimeZone.UTC)), request.performanceDate, _))

//            complete(response)

            onComplete(response) {
              case Success(rs: ReservationStatus) => complete(rs)
              case Failure(ex: Throwable) => complete(StatusCodes.BadRequest, JsObject("error" -> JsString(ex.getMessage)))
            }
          }
        }
      }
  }
}
