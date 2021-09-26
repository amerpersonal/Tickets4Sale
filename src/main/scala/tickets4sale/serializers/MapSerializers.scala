package tickets4sale.serializers

import spray.json.{JsArray, JsObject, JsValue, RootJsonFormat}
import tickets4sale.models.{PerformanceInventory, Show}
import spray.json._

object MapSerializers {
  import PerformanceInventorySerializer._
  import ShowSerializer._

  implicit object MapSerializer extends RootJsonFormat[Map[String, Seq[PerformanceInventory]]] {

    def read(js: JsValue): Map[String, Seq[PerformanceInventory]] = ???

    def write(m: Map[String, Seq[PerformanceInventory]]): JsObject = {
      val r = m.toSeq.map { case (k, v) =>
        val inventories: Seq[JsValue] = v.map(_.toJson)

        (k, JsArray(inventories: _*))
      }

      JsObject(r: _*)
    }
  }
}
