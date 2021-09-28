package tickets4sale.serializers

import spray.json.{DefaultJsonProtocol, JsObject, JsString, JsValue, RootJsonFormat}
import tickets4sale.behaviors.Inventory.ReservationCompleted

object ReservationCompletedSerializer {
  implicit object CustomReservationCompletedSerializer extends RootJsonFormat[ReservationCompleted] {
    implicit def read(js: JsValue): ReservationCompleted = ???

    implicit def write(model: ReservationCompleted): JsValue = {
      JsObject(
        "title" -> JsString(model.title)
      )
    }
  }

}
