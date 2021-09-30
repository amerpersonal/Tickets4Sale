package tickets4sale.serializers

import spray.json.{JsNumber, JsObject, JsString, JsValue, RootJsonFormat}
import tickets4sale.behaviors.Inventory.{ReservationFailure, ReservationStatus, ReservationSuccess}

object ReservationFailureSerializer {
  implicit object CustomReservationFailureSerializer extends RootJsonFormat[ReservationFailure] {
    implicit def read(js: JsValue): ReservationFailure = ???

    implicit def write(model: ReservationFailure): JsValue = {
      JsObject(
        "error" -> JsString(model.err.getMessage)
      )
    }
  }
}
