package tickets4sale.api

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.util.Timeout
import tickets4sale.behaviors.Inventory
import tickets4sale.config.Config
import tickets4sale.repository.{ShowCSVRepository, TicketOrderDatabaseRepository}
import tickets4sale.services.TicketOrderServiceFactory

import scala.concurrent.duration._
import scala.util.{Failure, Success}

object WebServer extends Validators with Config {
  def main(args: Array[String]): Unit = {
    val guardian = Behaviors.setup[Nothing] { context =>

//      val inventoryActor = context.spawn(Inventory(), name = "InventoryActor")

      val inventory = new Inventory() with TicketOrderServiceFactory with TicketOrderDatabaseRepository with ShowCSVRepository
      val inventoryActor = context.spawn(inventory.apply(), name = "InventoryActor")

      context.watch(inventoryActor)

      implicit val actorSystem: ActorSystem[_] = context.system

      implicit val ec = context.system.executionContext

      implicit val timeout = Timeout(4.seconds)

      val router = new Router(inventoryActor)

      val futureBinding = Http(context.system).newServerAt(serverHost, serverPort).bind(router.routes)

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
