package tickets4sale.serializers.requests

import org.joda.time.format.ISODateTimeFormat
import spray.json.{JsObject, JsString, JsValue, RootJsonFormat}
import tickets4sale.models.requests.ReserveTicketRequest
import tickets4sale.utils.DateUtils

object ReserveTicketRequestSerializer {
  implicit object CustomReserveTicketRequestSerializer extends RootJsonFormat[ReserveTicketRequest] {
    implicit def read(js: JsValue): ReserveTicketRequest = {
      val fields = js.asJsObject.fields

      val title = fields.get("title") match {
        case Some(v: JsString) => v.value
        case _ => throw new Throwable(s"invalid or missing ${fields.get("title")} title format")
      }

      val performanceDate = fields.get("performance_date") match {
        case Some(v: JsString) => DateUtils.parseDate(v.value)
        case _ => throw new Throwable(s"invalid or missing performance_date ${fields.get("performance_date")}")
      }

      ReserveTicketRequest(title, performanceDate)
    }

    implicit def write(req: ReserveTicketRequest): JsObject = ???
  }
}