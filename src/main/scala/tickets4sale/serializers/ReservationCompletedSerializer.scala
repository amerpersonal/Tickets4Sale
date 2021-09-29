package tickets4sale.serializers

import spray.json.{DefaultJsonProtocol, JsNumber, JsObject, JsString, JsValue, RootJsonFormat}
import tickets4sale.behaviors.Inventory.ReservationCompleted

object ReservationCompletedSerializer {
  implicit object CustomReservationCompletedSerializer extends RootJsonFormat[ReservationCompleted] {
    implicit def read(js: JsValue): ReservationCompleted = ???

    implicit def write(model: ReservationCompleted): JsValue = {
      JsObject(
        "title" -> JsString(model.title),
        "reservation_date" -> JsString(model.reservationDate.toString()),
        "performance_date" -> JsString(model.performanceDate.toString()),
        "tickets_left" -> JsNumber(model.ticketsLeft)
      )
    }
  }

}
