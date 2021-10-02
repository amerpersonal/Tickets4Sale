package api

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.util.Timeout
import org.joda.time.LocalDate
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import spray.json._
import tickets4sale.api.Router
import tickets4sale.behaviors.Inventory
import tickets4sale.behaviors.Inventory.{FullPerformanceInventory, ReservationFailure}
import tickets4sale.config.Config
import tickets4sale.models.requests.ReserveTicketRequest
import tickets4sale.models.{Halls, TicketSaleStates}
import tickets4sale.repository.TicketOrderMapRepository
import tickets4sale.serializers.ReservationFailureSerializer.CustomReservationFailureSerializer
import tickets4sale.serializers.requests.ReserveTicketRequestSerializer._
import tickets4sale.services.TicketOrderServiceFactory
import tickets4sale.utils.DateUtils
import scala.concurrent.duration._

class ApiSpec extends AnyWordSpec with ScalatestRouteTest with should.Matchers with ScalaFutures with Config {
  import tickets4sale.serializers.FullPerformanceInventorySerializers._

  lazy val testKit = ActorTestKit()

  implicit val timeout = Timeout(3.seconds)

  implicit def default(implicit system: akka.actor.ActorSystem) = RouteTestTimeout(40.seconds)

  implicit val as: akka.actor.typed.ActorSystem[Nothing] = system.toTyped

  val inventory = new Inventory() with TicketOrderServiceFactory with TicketOrderMapRepository

  val inventoryActor = testKit.spawn(inventory.apply(), name = "InventoryActor")

  val router = new Router(inventoryActor)


  "return success on root GET request" in {
    Get("/api/v1/performance_inventory?query_date=2021-11-25&performance_date=2021-12-05") ~> Route.seal(router.routes) ~> check {
      status shouldEqual StatusCodes.OK
      val inventory = entityAs[String].parseJson.convertTo[FullPerformanceInventory].inventory

      inventory.size shouldEqual 3
    }
  }

  "fail on reserving ticket for invalid show" in {
    val req = ReserveTicketRequest("bla bla", LocalDate.now())
    Post("/api/v1/reserve_ticket", HttpEntity(ContentTypes.`application/json`, req.toJson.toString())) ~> Route.seal(router.routes) ~> check {

      status shouldEqual StatusCodes.BadRequest

      entityAs[String].parseJson.convertTo[ReservationFailure].err.getMessage shouldEqual "Show not exists"
    }
  }

  "fail on reserving ticket for non running performance" in {
    val queryDate = "2021-11-25"
    val performanceDate = "2021-12-05"
    Get(s"/api/v1/performance_inventory?query_date=${queryDate}&performance_date=${performanceDate}") ~> Route.seal(router.routes) ~> check {
      status shouldEqual StatusCodes.OK
      val inventory = entityAs[String].parseJson.convertTo[FullPerformanceInventory].inventory

      val show = inventory.values.head.head.show

      val req = ReserveTicketRequest(show.title, show.openingDay.plusDays(300))
      Post("/api/v1/reserve_ticket", HttpEntity(ContentTypes.`application/json`, req.toJson.toString())) ~> Route.seal(router.routes) ~> check {

        status shouldEqual StatusCodes.BadRequest
        entityAs[String].parseJson.convertTo[ReservationFailure].err.getMessage shouldEqual "Show not running"
      }
    }
  }

  "can reserve up to 10 tickets for a performance taking place in a big hall" in {
    val queryDate = DateUtils.parseDate("2021-11-25")
    val performanceDate = DateUtils.parseDate("2021-12-05")

    Get(s"/api/v1/performance_inventory?query_date=${queryDate}&performance_date=${performanceDate}") ~> Route.seal(router.routes) ~> check {

      status shouldEqual StatusCodes.OK
      val inventory = responseAs[String].parseJson.convertTo[FullPerformanceInventory].inventory

      val performanceShow = inventory.values.flatten.find { i =>
        i.status == TicketSaleStates.OpenForSale &&
          Halls.performanceHall(i.show, performanceDate).getOrElse(0) == Halls.Big &&
          i.show.openingDay.minusDays(saleEndsBefore).isAfter(queryDate)
      }.get.show

      val req = ReserveTicketRequest(performanceShow.title, performanceDate, Some(queryDate))

      (0 until Halls.Big.ticketsPerDay).foreach { _ =>
        Post("/api/v1/reserve_ticket", HttpEntity(ContentTypes.`application/json`, req.toJson.toString())) ~> Route.seal(router.routes) ~> check {
          status shouldEqual StatusCodes.OK
        }
      }

      Post("/api/v1/reserve_ticket", HttpEntity(ContentTypes.`application/json`, req.toJson.toString())) ~> Route.seal(router.routes) ~> check {
        status shouldEqual StatusCodes.BadRequest

        entityAs[String].parseJson.convertTo[ReservationFailure].err.getMessage shouldEqual "No tickets left for ordering on this day"
      }
    }
  }

  "can reserve up to 5 tickets for a performance taking place in a big hall" in {
    val queryDate = DateUtils.parseDate("2021-11-25")
    val performanceDate = DateUtils.parseDate("2021-12-05")

    Get(s"/api/v1/performance_inventory?query_date=${queryDate}&performance_date=${performanceDate}") ~> Route.seal(router.routes) ~> check {

      status shouldEqual StatusCodes.OK
      val inventory = responseAs[String].parseJson.convertTo[FullPerformanceInventory].inventory

      val performanceShow = inventory.values.flatten.find { i =>
        i.status == TicketSaleStates.OpenForSale &&
          Halls.performanceHall(i.show, performanceDate).getOrElse(0) == Halls.Big &&
          i.show.openingDay.minusDays(saleEndsBefore).isAfter(queryDate)
      }.get.show

      val pd = performanceShow.openingDay.plusDays(70)
      val qd = pd.minusDays(15)

      val req = ReserveTicketRequest(performanceShow.title, pd, Some(qd))

      (0 until Halls.Small.ticketsPerDay).foreach { _ =>
        Post("/api/v1/reserve_ticket", HttpEntity(ContentTypes.`application/json`, req.toJson.toString())) ~> Route.seal(router.routes) ~> check {
          status shouldEqual StatusCodes.OK
        }
      }

      Post("/api/v1/reserve_ticket", HttpEntity(ContentTypes.`application/json`, req.toJson.toString())) ~> Route.seal(router.routes) ~> check {
        status shouldEqual StatusCodes.BadRequest

        entityAs[String].parseJson.convertTo[ReservationFailure].err.getMessage shouldEqual "No tickets left for ordering on this day"
      }
    }
  }
}
