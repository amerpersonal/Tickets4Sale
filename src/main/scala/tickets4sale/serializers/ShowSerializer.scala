package tickets4sale.serializers

import spray.json.{DefaultJsonProtocol, JsObject, JsString, JsValue, RootJsonFormat}
import tickets4sale.models.{Genres, PerformanceInventory, Show}
import tickets4sale.serializers.PerformanceInventorySerializer.jsonFormat4
import tickets4sale.utils.DateUtils

object ShowSerializer {

  implicit object CustomShowSerializer extends RootJsonFormat[Show] {
    //  implicit val showSerializer = jsonFormat3(Show.apply)

    implicit def read(js: JsValue): Show = {
      val fields = js.asJsObject().fields

      val title = fields.get("title") match {
        case Some(v: JsString) => v.value
        case _ => throw new Throwable(s"invalid or missing ${fields.get("title")} title format")
      }

      val openingDay = fields.get("opening_day") match {
        case Some(v: JsString) => DateUtils.parseDate(v.value)
        case _ => throw new Throwable(s"invalid or missing opening_day ${fields.get("opening_day")}")
      }

      val genre = fields.get("genre") match {
        case Some(v: JsString) => v.value
        case _ => throw new Throwable(s"invalid or missing ${fields.get("genre")} genre format")
      }

      println(genre)
      Show(title, openingDay, Genres.fromName(genre).get)
    }

    implicit def write(s: Show): JsObject = {
      JsObject(
        "title" -> JsString(s.title),
        "opening_day" -> JsString(s.openingDay.toString()),
        "genre" -> JsString(s.genre.name)
      )
    }
  }
}
