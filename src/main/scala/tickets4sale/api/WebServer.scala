package tickets4sale.api

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.http.scaladsl.Http
import akka.util.Timeout
import tickets4sale.behaviors.Inventory

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.RouteResult
import akka.stream.Materializer
import RouteResult._
import akka.actor.ClassicActorSystemProvider
import tickets4sale.repository.{ShowCSVRepository, TicketOrderDatabaseRepository}
import tickets4sale.services.TicketOrderServiceFactory

object WebServer extends App with Api {
  val serverInterface = "0.0.0.0"
  val serverPort = 8080

  implicit val timeout  = Timeout(3.seconds)


//  implicit val mat = Materializer.matFromSystem(new ClassicActorSystemProvider {
//    override def classicSystem: ActorSystem = actorSystem
//  })

//  implicit val actorSystem: ActorSystem[Inventory.CalculatePerformanceInventory] = ActorSystem(Inventory(), "inventory")


  val system = akka.actor.ActorSystem("ClassicToTypedSystem")
  implicit val materializer = Materializer(system)

  val as: ActorSystem[_] = system.toTyped


  implicit val provider = new ClassicActorSystemProvider {
    override def classicSystem: akka.actor.ActorSystem = system
  }
//
//
//
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
//
//
  val inventory = new Inventory() with TicketOrderServiceFactory with TicketOrderDatabaseRepository with ShowCSVRepository
  val inventoryActor: ActorRef[Inventory.InventoryMessage] = as.systemActorOf(inventory.apply(), "inventory")


  println(s"Bounding HTTP server to ${serverInterface}: ${serverPort}")


  //RouteResult.routeToFunction()

//  Http(as).newServerAt(serverInterface, serverPort).bind(routes)



}
