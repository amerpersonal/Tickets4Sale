package tickets4sale.api

import akka.http.scaladsl.server.Directives.{provide, reject, _}
import akka.http.scaladsl.server.{Directive1, MalformedQueryParamRejection}
import org.joda.time.LocalDate
import tickets4sale.utils.DateUtils

import scala.util.{Success, Try}

trait Validators {
  def validateInventoryDates(first: String, second: String): Directive1[(LocalDate, LocalDate)] = {
    parameters(first, second).tflatMap { case (qd, pd) =>

      (Try(DateUtils.parseDate(qd)), Try(DateUtils.parseDate(pd))) match {
        case (Success(queryDate), Success(performanceDate)) => provide(queryDate, performanceDate)
        case (_, _) => reject(MalformedQueryParamRejection("date", "dates invalid"))
      }
    }
  }

}
