package tickets4sale.api

import akka.http.scaladsl.server.{Directive1, MalformedQueryParamRejection, directives}
import akka.http.scaladsl.server.Directives.{as, entity, extractUri, post, provide, reject}
import spray.json.JsObject
import akka.http.scaladsl.server.Directives._
import org.joda.time.LocalDate
import tickets4sale.repository.ShowCSVRepository
import tickets4sale.services.ShowsService
import tickets4sale.utils.DateUtils

import scala.util.{Success, Try}


trait Validators extends ShowsService with ShowCSVRepository {
//  def validateRequestId: Directive1[(String, Map[String, Any])] = {
//    extractUri flatMap { uri =>
//      ((post | delete | put | patch) & entity(as[JsObject])).flatMap { mapJson =>
//
//        val body = jsObjectToMap(mapJson)
//        logMessage(s"Request came to url ${uri.toString()} with body: ${body}")
//
//        val requestBody = (body.get("request_id"), body.get("data"))
//
//        requestBody match {
//          case (Some(requestId: String), Some(data: Map[String, Any] @unchecked)) if isValidRequestId(requestId) => {
//            logApiRequest(requestId, s"Request ${requestId} came to url ${uri.toString()} with data: ${data}")
//            provide(requestId, data)
//          }
//          case _ => reject(ApiErrors.InvalidRequest)
//        }
//      }
//    }
//  }

  def validateInventoryDates(first: String, second: String): Directive1[(LocalDate, LocalDate)] = {
    parameters(first, second).tflatMap { case (qd, pd) =>

      (Try(DateUtils.parseDate(qd)), Try(DateUtils.parseDate(pd))) match {
        case (Success(queryDate), Success(performanceDate)) => provide(queryDate, performanceDate)
        case (_, _) => reject(MalformedQueryParamRejection("date", "dates invalid"))
      }
    }
  }


}
