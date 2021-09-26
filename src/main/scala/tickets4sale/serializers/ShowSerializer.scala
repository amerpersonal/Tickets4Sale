package tickets4sale.serializers

import spray.json.{DefaultJsonProtocol, JsObject, JsString, JsValue, RootJsonFormat}
import tickets4sale.models.{PerformanceInventory, Show}
import tickets4sale.serializers.PerformanceInventorySerializer.jsonFormat4

object ShowSerializer {

  implicit object CustomShowSerializer extends RootJsonFormat[Show] {
    //  implicit val showSerializer = jsonFormat3(Show.apply)

    implicit def read(js: JsValue): Show = ???

    implicit def write(s: Show): JsObject = {
      JsObject(
        "title" -> JsString(s.title),
        "opening_day" -> JsString(s.openingDay.toString())
      )
    }
  }
}
