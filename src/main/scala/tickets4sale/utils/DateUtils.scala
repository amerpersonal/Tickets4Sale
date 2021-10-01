package tickets4sale.utils

import org.joda.time.LocalDate
import org.joda.time.format.ISODateTimeFormat

object DateUtils {
  val dateFormat = ISODateTimeFormat.date()

  def parseDate(str: String): LocalDate = {
    LocalDate.parse(str, dateFormat)
  }
}
