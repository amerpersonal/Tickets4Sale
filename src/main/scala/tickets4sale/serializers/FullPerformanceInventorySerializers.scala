package tickets4sale.serializers

import spray.json.{DefaultJsonProtocol, JsArray, JsObject, JsValue, RootJsonFormat}
import tickets4sale.behaviors.Inventory.FullPerformanceInventory
import tickets4sale.models.{PerformanceInventory, Show}
import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

object FullPerformanceInventorySerializers {
  import MapSerializers._

  implicit object FullPerformanceInventorySerializer extends RootJsonFormat[FullPerformanceInventory] {

    def read(js: JsValue): FullPerformanceInventory = ???

    def write(inv: FullPerformanceInventory): JsObject = {
      JsObject(("inventory", inv.inventory.toJson))
    }
  }
}
