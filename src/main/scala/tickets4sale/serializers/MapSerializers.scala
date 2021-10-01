package tickets4sale.serializers

import spray.json.{JsArray, JsObject, JsValue, RootJsonFormat, _}
import tickets4sale.models.PerformanceInventory

object MapSerializers {

  import PerformanceInventorySerializer._

  implicit object MapSerializer extends RootJsonFormat[Map[String, Seq[PerformanceInventory]]] {

    def read(js: JsValue): Map[String, Seq[PerformanceInventory]] = {

      js.asJsObject.fields.collect { case (field, JsArray(elements)) =>
        field -> elements.map(PerformanceInventorySerializer.performanceInventorySerializer.read(_))
      }
    }

    def write(m: Map[String, Seq[PerformanceInventory]]): JsObject = {
      val r = m.toSeq.map { case (k, v) =>
        val inventories: Seq[JsValue] = v.map(_.toJson)

        (k, JsArray(inventories: _*))
      }

      JsObject(r: _*)
    }
  }

}
