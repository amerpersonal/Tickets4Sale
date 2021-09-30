package api

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.util.Timeout
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import spray.json._
import tickets4sale.api.Router
import tickets4sale.behaviors.Inventory
import tickets4sale.repository.{ShowCSVRepository, TicketOrderDatabaseRepository}
import tickets4sale.serializers.FullPerformanceInventorySerializers
import tickets4sale.services.TicketOrderServiceFactory
import scala.concurrent.duration._

class ApiSpec extends AnyWordSpec with ScalatestRouteTest with should.Matchers {

  lazy val testKit = ActorTestKit()

  implicit val timeout = Timeout(3.seconds)

  implicit def default(implicit system: akka.actor.ActorSystem) = RouteTestTimeout(40.seconds)

  implicit val as: akka.actor.typed.ActorSystem[Nothing] = system.toTyped

  val inventory = new Inventory() with TicketOrderServiceFactory with TicketOrderDatabaseRepository with ShowCSVRepository

  val inventoryActor = testKit.spawn(inventory.apply(), name = "InventoryActor")

  val router = new Router(inventoryActor)


  "return success on root GET request" in {
    Get("/api/v1/performance_inventory?query_date=2021-11-25&performance_date=2021-12-05") ~> Route.seal(router.routes) ~> check {

      val map = FullPerformanceInventorySerializers.FullPerformanceInventorySerializer.read(responseAs[String].parseJson)

      map.inventory.size shouldEqual 3
    }
  }
}
