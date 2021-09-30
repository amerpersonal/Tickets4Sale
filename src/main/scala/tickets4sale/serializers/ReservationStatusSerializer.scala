package tickets4sale.serializers

import spray.json.{DefaultJsonProtocol, JsNumber, JsObject, JsString, JsValue, RootJsonFormat}
import tickets4sale.behaviors.Inventory.ReservationStatus

object ReservationStatusSerializer {
  implicit object CustomReservationCompletedSerializer extends RootJsonFormat[ReservationStatus] {
    implicit def read(js: JsValue): ReservationStatus = ???

    implicit def write(model: ReservationStatus): JsValue = {
      model.err match {
        case Some(err) => JsObject("error" -> JsString("err"))
        case None => JsObject(
          "title" -> JsString(model.title),
          "reservation_date" -> JsString(model.reservationDate.toString()),
          "performance_date" -> JsString(model.performanceDate.toString()),
          "tickets_left" -> JsNumber(model.ticketsLeft)
        )
      }

    }
  }

}
