package tickets4sale.models

import org.joda.time.{Days, LocalDate}
import tickets4sale.config.Config
import tickets4sale.models.Halls.Big.config

import scala.concurrent.duration.Duration


object Halls {
//  case class Hall(name: String, capacity: Int, dayStart: Int, dayEnd: Int)
//
//  val Big = Hall("big", 200, 0, 60)
//
//  val Small = Hall("small", 100, 60, 100)



  sealed trait Hall extends Config {
    def capacity: Int
    def dayStart: Int
    def dayEnd: Int
    def ticketsPerDay: Int

    def duration: Int = dayEnd - dayStart

    def isShowRunCurrently(show: Show, performanceDate: LocalDate): Boolean = {
      val showRunningForDays = Days.daysBetween(show.openingDay, performanceDate).getDays + 1

      (showRunningForDays < 0 && dayStart == 0) || (showRunningForDays >= dayStart && showRunningForDays <= dayEnd)
    }


    def ticketsLeft(queryDate: LocalDate, performanceDate: LocalDate): Int = {
      val remainingDaysUntilPerformance = Days.daysBetween(queryDate, performanceDate).getDays + 1

      if (remainingDaysUntilPerformance < saleEndsBefore) 0
      else if (remainingDaysUntilPerformance  > saleStartsBefore) capacity
      else (remainingDaysUntilPerformance - saleEndsBefore) * ticketsPerDay

    }

    def ticketsAvailable(queryDate: LocalDate, performanceDate: LocalDate): Int = {
      val remainingDaysUntilPerformance = Days.daysBetween(queryDate, performanceDate).getDays + 1
      if (remainingDaysUntilPerformance < saleEndsBefore || remainingDaysUntilPerformance > saleStartsBefore) 0
      else ticketsPerDay

    }
  }

  case object Big extends Hall {
    val capacity = config.getInt("tickets4sale.halls.big.capacity")
    val dayStart = config.getInt("tickets4sale.halls.big.day-start")
    val dayEnd = config.getInt("tickets4sale.halls.big.day-end")
    val ticketsPerDay = config.getInt("tickets4sale.halls.big.tickets-sold-per-day")
  }

  case object Small extends Hall {
    val capacity = config.getInt("tickets4sale.halls.small.capacity")
    val dayStart = config.getInt("tickets4sale.halls.small.day-start")
    val dayEnd = config.getInt("tickets4sale.halls.small.day-end")
    val ticketsPerDay = config.getInt("tickets4sale.halls.small.tickets-sold-per-day")
  }


  val halls = List(Big, Small)

  def performanceHall(show: Show, performanceDate: LocalDate): Option[Hall] = {
    halls.find(_.isShowRunCurrently(show, performanceDate))
  }

}
