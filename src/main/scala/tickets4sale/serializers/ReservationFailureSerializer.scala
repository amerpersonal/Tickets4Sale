package tickets4sale.serializers

import spray.json.{JsObject, JsString, JsValue, RootJsonFormat}
import tickets4sale.behaviors.Inventory.ReservationFailure

object ReservationFailureSerializer {
  import SerializationHelpers._
  import spray.json.DefaultJsonProtocol._

  implicit object CustomReservationFailureSerializer extends RootJsonFormat[ReservationFailure] {
    implicit def read(js: JsValue): ReservationFailure = {
      ReservationFailure(new Throwable(js.asJsObject.readValue[String]("error")))
    }

    implicit def write(model: ReservationFailure): JsValue = {
      JsObject(
        "error" -> JsString(model.err.getMessage)
      )
    }
  }
}
