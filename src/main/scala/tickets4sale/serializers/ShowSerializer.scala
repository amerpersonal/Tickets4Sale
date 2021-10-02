package tickets4sale.serializers

import org.joda.time.LocalDate
import spray.json.{DefaultJsonProtocol, JsObject, JsString, JsValue, JsonReader, RootJsonFormat}
import tickets4sale.models.{Genres, Show}
import tickets4sale.utils.DateUtils
import scala.util.Try

object ShowSerializer {

  import DefaultJsonProtocol._

  implicit object CustomShowSerializer extends RootJsonFormat[Show] {
    import SerializationHelpers._

    implicit def read(js: JsValue): Show = {
      val jsObj = js.asJsObject()

      val title = jsObj.readValue[String]("title")
      val openingDay = jsObj.readAndTransformValue[LocalDate]("opening_day", DateUtils.parseDate)
      val genre = jsObj.readValue[String]("genre")

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
