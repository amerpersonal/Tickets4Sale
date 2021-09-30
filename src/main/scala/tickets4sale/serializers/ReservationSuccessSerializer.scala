package tickets4sale.serializers

import spray.json.{DefaultJsonProtocol, JsNumber, JsObject, JsString, JsValue, RootJsonFormat}
import tickets4sale.behaviors.Inventory.{ReservationStatus, ReservationSuccess}

object ReservationSuccessSerializer {

  implicit object CustomReservationSuccessSerializer extends RootJsonFormat[ReservationSuccess] {
    implicit def read(js: JsValue): ReservationSuccess = ???

    implicit def write(model: ReservationSuccess): JsValue = {
      JsObject(
        "title" -> JsString(model.title),
        "reservation_date" -> JsString(model.reservationDate.toString()),
        "performance_date" -> JsString(model.performanceDate.toString()),
        "tickets_left" -> JsNumber(model.ticketsLeft)
      )
    }
  }
}
