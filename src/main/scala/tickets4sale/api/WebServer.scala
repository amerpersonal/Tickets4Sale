package tickets4sale.api

import akka.actor.typed.{ActorRef, ActorSystem, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import tickets4sale.behaviors.Inventory
import tickets4sale.behaviors.Inventory._
import akka.actor.typed.scaladsl.AskPattern._

import scala.concurrent.duration._
import scala.concurrent.Future
import akka.http.scaladsl.server
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import org.joda.time.{DateTimeZone, LocalDate}
import spray.json.{JsObject, JsString}
import tickets4sale.models.requests.ReserveTicketRequest

import scala.util.{Failure, Success}
import tickets4sale.serializers.requests.ReserveTicketRequestSerializer._
import tickets4sale.serializers.ReservationCompletedSerializer._

object WebServer extends Validators {
  def main(args: Array[String]): Unit = {
    val guardian = Behaviors.setup[Nothing] { context =>
      import tickets4sale.serializers.FullPerformanceInventorySerializers._

      val inventoryActor = context.spawn(Inventory(), name = "InventoryActor")

      context.watch(inventoryActor)


      implicit val actorSystem: ActorSystem[_] = context.system

      implicit val ec = context.system.executionContext

      implicit val timeout = Timeout(4.seconds)

      def executeAndShowResponse(request: Future[Any]): Route = {
        onComplete(request) { response =>
          response match {
            case Success(r: ReservationCompleted) => complete(StatusCodes.OK, r)
            case Success(ReservationFailed(err)) => complete(StatusCodes.BadRequest, JsObject("error" -> JsString(err.getMessage)))
            case _ => complete(StatusCodes.InternalServerError, JsObject("error" -> JsString("Error executing request")))
          }
        }
      }

      val performanceInventoryRoute = get {
        path("performance_inventory") {
          validateInventoryDates("query_date", "performance_date") { case (queryDate, performanceDate) =>

            val response: Future[FullPerformanceInventory] = inventoryActor.ask(CalculatePerformanceInventory(queryDate, performanceDate, _))

            complete(response)
          }
        }
      }


      val orderTicketRoute = post {
        path("show" / "reserve_ticket") {
          entity(as[ReserveTicketRequest]) { request =>

            val r: Future[InventoryMessage] = inventoryActor.ask(ReserveTicket(request.title, request.queryDate.getOrElse(LocalDate.now(DateTimeZone.UTC)), request.performanceDate, _))

            executeAndShowResponse(r)

          }
        }
      }


      val routes = pathPrefix("api" / "v1") {
        performanceInventoryRoute ~ orderTicketRoute
      }


      val server = Http(context.system).newServerAt("0.0.0.0", 8080).bind(routes)

      server.onComplete {
        case Success(binding) =>
          val address = binding.localAddress
          actorSystem.log.info("Server online at http://{}:{}/",
            address.getHostString,
            address.getPort)
        case Failure(ex) =>
          actorSystem.log.error("Failed to bind HTTP endpoint, terminating system", ex)
          actorSystem.terminate()
      }

      Behaviors.same
    }

    val system = ActorSystem[Nothing](guardian, "Guardian")

  }
}
