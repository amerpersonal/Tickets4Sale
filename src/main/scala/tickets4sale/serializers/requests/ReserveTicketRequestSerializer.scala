package tickets4sale.serializers.requests

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

      val queryDate = fields.get("query_date") match {
        case Some(v: JsString) => Some(DateUtils.parseDate(v.value))
        case None => None
        case _ => throw new Throwable(s"invalid or missing performance_date ${fields.get("performance_date")}")
      }

      ReserveTicketRequest(title, performanceDate, queryDate)
    }

    implicit def write(req: ReserveTicketRequest): JsObject = {
      val baseFields = List(
        ("title", JsString(req.title)),
        ("performance_date", JsString(req.performanceDate.toString()))
      )

      val fields = req.queryDate.map(qa => ("query_date", JsString(qa.toString())) :: baseFields).getOrElse(baseFields)

      JsObject(fields: _*)
    }
  }
}
