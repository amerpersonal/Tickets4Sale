package tickets4sale.utils

import java.text.SimpleDateFormat

import org.joda.time.LocalDate
import org.joda.time.format.ISODateTimeFormat

object DateUtils {
//  val dateFormat = new SimpleDateFormat("yyyy-MM-dd")


  def parseDate(str: String): LocalDate = {
    LocalDate.parse(str, ISODateTimeFormat.date())
  }
}
