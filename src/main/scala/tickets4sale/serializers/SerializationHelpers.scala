package tickets4sale.serializers

import spray.json.{JsObject, JsonReader}
import scala.util.Try

object SerializationHelpers {
  implicit class RichJsObject(val underlying: JsObject) extends AnyVal {
    def readValue[T](field: String)(implicit reader: JsonReader[T]): T = {
      underlying.fields.get(field).flatMap { jsValue =>
        Try(jsValue.convertTo[T]).toOption
      }.getOrElse(throw new Throwable(s"${field} missing or invalid"))
    }

    def readAndTransformValue[T](field: String, transform: String => T)(implicit reader: JsonReader[String]): T = {
      underlying.fields.get(field).flatMap { jsValue =>
        Try(jsValue.convertTo[String]).toOption
      }.flatMap { v =>
        Try(transform(v)).toOption
      }.getOrElse(throw new Throwable(s"${field} missing or invalid"))
    }
  }
}
