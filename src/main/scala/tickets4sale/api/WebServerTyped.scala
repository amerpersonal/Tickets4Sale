package tickets4sale.api

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import tickets4sale.behaviors.Inventory
import tickets4sale.behaviors.Inventory.{CalculatePerformanceInventory, FullPerformanceInventory}
import akka.actor.typed.scaladsl.AskPattern._

import scala.concurrent.duration._
import scala.concurrent.Future
import akka.http.scaladsl.server
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.util.Timeout
import org.joda.time.LocalDate

import scala.util.{Failure, Success}

object WebServerTyped extends Validators {
  def main(args: Array[String]): Unit = {
    val guardian = Behaviors.setup[Nothing] { context =>
      import tickets4sale.serializers.FullPerformanceInventorySerializers._

      val inventoryActor = context.spawn(Inventory(), name = "InventoryActor")

      context.watch(inventoryActor)


      implicit val actorSystem: ActorSystem[_] = context.system

      implicit val ec = context.system.executionContext

      implicit val timeout = Timeout(4.seconds)

      val routes = pathPrefix("api" / "v1") {
        get {
          path("performance_inventory") {
            validateInventoryDates("query_date", "performance_date") { case (queryDate, performanceDate) =>

              val response: Future[FullPerformanceInventory] = inventoryActor.ask(CalculatePerformanceInventory(queryDate, performanceDate, _))

              complete(response)
            }
          }
        }
      }

      val futureBinding = Http(context.system).newServerAt("0.0.0.0", 8080).bind(routes)

      futureBinding.onComplete {
        case Success(binding) =>
          val address = binding.localAddress
          actorSystem.log.info("Server online at http://{}:{}/",
            address.getHostString,
            address.getPort)
        case Failure(ex) =>
          actorSystem.log.error("Failed to bind HTTP endpoint, terminating system", ex)
          actorSystem.terminate()
      }

      Behaviors.empty
    }

    val system = ActorSystem[Nothing](guardian, "Guardian")

  }


}
