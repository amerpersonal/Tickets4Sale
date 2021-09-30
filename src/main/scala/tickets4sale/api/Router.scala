package tickets4sale.api

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import org.joda.time.{DateTimeZone, LocalDate}
import tickets4sale.behaviors.Inventory._
import tickets4sale.models.requests.ReserveTicketRequest
import scala.concurrent.Future
import scala.util.Success

class Router(inventoryActor: ActorRef[InventoryMessage])(implicit system: ActorSystem[_], timeout: Timeout) extends Validators {
  import tickets4sale.serializers.FullPerformanceInventorySerializers._
  import tickets4sale.serializers.ReservationFailureSerializer._
  import tickets4sale.serializers.ReservationSuccessSerializer._
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

            onComplete(response){
              case Success(rs: ReservationSuccess) => complete(rs)
              case Success(rf: ReservationFailure) => complete(StatusCodes.BadRequest, rf)
            }
          }
        }
      }
  }
}
