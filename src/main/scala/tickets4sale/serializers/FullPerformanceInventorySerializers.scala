package tickets4sale.serializers

import spray.json.{JsObject, JsValue, RootJsonFormat, _}
import tickets4sale.behaviors.Inventory.FullPerformanceInventory

object FullPerformanceInventorySerializers {
  import MapSerializers._

  implicit object FullPerformanceInventorySerializer extends RootJsonFormat[FullPerformanceInventory] {

    def read(js: JsValue): FullPerformanceInventory = {
      FullPerformanceInventory(MapSerializers.MapSerializer.read(js.asJsObject.fields("inventory")))
    }

    def write(inv: FullPerformanceInventory): JsObject = {
      JsObject(("inventory", inv.inventory.toJson))
    }
  }

}
