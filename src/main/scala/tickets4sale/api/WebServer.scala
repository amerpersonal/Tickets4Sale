package tickets4sale.api

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.stream.Materializer
import akka.util.Timeout
import tickets4sale.behaviors.Inventory
import akka.actor.typed.scaladsl.adapter._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._


object WebServer extends App with Api {
  val serverInterface = "0.0.0.0"
  val serverPort = 8080

  implicit val timeout  = Timeout(3.seconds)

//  implicit val mat = Materializer.matFromSystem(new ClassicActorSystemProvider {
//    override def classicSystem: ActorSystem = actorSystem
//  })

  implicit val actorSystem: ActorSystem[Inventory.CalculatePerformanceInventory] = ActorSystem(Inventory(), "inventory")



//  implicit val actorSystem: ActorSystem[_] = ActorSystem(Inventory.apply(), "inventory")

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  val inventoryActor: ActorRef[Inventory.CalculatePerformanceInventory] = actorSystem

  val responseActor: ActorRef[Inventory.CalculatePerformanceInventory] = actorSystem

  println(s"Bounding HTTP server to ${serverInterface}: ${serverPort}")
  Http(actorSystem).newServerAt(serverInterface, serverPort).bind(routes)



}
