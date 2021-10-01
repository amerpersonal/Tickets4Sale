package tickets4sale.serializers

import spray.json.{JsObject, JsString, JsValue, RootJsonFormat}
import tickets4sale.behaviors.Inventory.ReservationFailure

object ReservationFailureSerializer {
  implicit object CustomReservationFailureSerializer extends RootJsonFormat[ReservationFailure] {
    implicit def read(js: JsValue): ReservationFailure = {
      js.asJsObject.fields.get("error") match {
        case Some(err: JsString) => ReservationFailure(new Throwable(err.value))
        case _ => throw new Throwable("Serialization cannot be done")
      }
    }

    implicit def write(model: ReservationFailure): JsValue = {
      JsObject(
        "error" -> JsString(model.err.getMessage)
      )
    }
  }
}
