package unit

import org.joda.time.{DateTimeZone, LocalDate}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import tickets4sale.models.{Genres, PerformanceInventory, Show, TicketSaleState}

class ShowInventorySpec extends AnyFlatSpec with Matchers {
  import tickets4sale.utils.DateUtils._

  import scala.concurrent.ExecutionContext.Implicits.global

  it should "return None for performance day before show beginning" in {
    val openingDay = LocalDate.now(DateTimeZone.UTC)
    val show = Show("cats", openingDay, Genres.Drama)

    val performanceDate = openingDay.minusDays(2)
    val queryDate = performanceDate.minusDays(5)
    show.inventory(queryDate, performanceDate).map(_ shouldEqual None)
  }

  it should "return None for performance day after show ending" in {
    val openingDay = LocalDate.now(DateTimeZone.UTC)
    val show = Show("cats", openingDay, Genres.Drama)

    val performanceDate = openingDay.plusDays(100)
    val queryDate = LocalDate.now()
    show.inventory(queryDate, performanceDate).map(_ shouldEqual None)
  }

  it should "return correct inventory for a show when sale is not started" in {
    val openingDay = parseDate("2018-06-01")

    val show = Show("cats", openingDay, Genres.Drama)

    val queryDate = parseDate("2018-01-01")
    val performanceDate = parseDate("2018-07-01")

    show.inventory(queryDate, performanceDate).map(_  shouldEqual Some(PerformanceInventory(show, 200, 0, TicketSaleState.SaleNotStarted)))
  }

  it should "return correct inventory for a show when sale is open" in {
    val openingDay = parseDate("2018-06-01")

    val show = Show("cats", openingDay, Genres.Drama)

    val queryDate = parseDate("2018-08-01")
    val performanceDate = parseDate("2018-08-15")

    show.inventory(queryDate, performanceDate) shouldEqual Some(PerformanceInventory(show, 50, 5, TicketSaleState.OpenForSale))
  }


  it should "return correct inventory for a show when ticket sale is in the past" in {
    val openingDay = parseDate("2018-06-01")

    val show = Show("cats", openingDay, Genres.Drama)

    val queryDate = parseDate("2018-08-01")
    val performanceDate = parseDate("2018-07-15")

    show.inventory(queryDate, performanceDate).map(_ shouldEqual Some(PerformanceInventory(show, 0, 0, TicketSaleState.InThePast)))
  }

  it should "return correct inventory for a show when performance day is the first day running in big hall" in {
    val openingDay = parseDate("2018-06-01")

    val show = Show("cats", openingDay, Genres.Drama)

    val queryDate = openingDay.minusDays(30)
    val performanceDate = openingDay

    show.inventory(queryDate, performanceDate).map(_ shouldEqual Some(PerformanceInventory(show, 200, 0, TicketSaleState.SaleNotStarted)))
  }

  it should "return correct inventory for a show when performance day is the last day running in big hall" in {
    val openingDay = parseDate("2018-06-01")

    val show = Show("cats", openingDay, Genres.Drama)

    val performanceDate = openingDay.plusDays(59)
    val queryDate = performanceDate.minusDays(10)

    show.inventory(queryDate, performanceDate).map(_ shouldEqual Some(PerformanceInventory(show, 60, 10, TicketSaleState.OpenForSale)))
  }

  it should "return correct inventory for a show when performance day is the first day running in small hall" in {
    val openingDay = parseDate("2018-06-01")

    val show = Show("cats", openingDay, Genres.Drama)

    val queryDate = openingDay.minusDays(30)
    val performanceDate = openingDay.plusDays(60)

    show.inventory(queryDate, performanceDate).map(_ shouldEqual Some(PerformanceInventory(show, 100, 0, TicketSaleState.SaleNotStarted)))
  }

  it should "return correct inventory for a show when performance day is the last day" in {
    val openingDay = parseDate("2018-06-01")

    val show = Show("cats", openingDay, Genres.Drama)

    val queryDate = openingDay.plusDays(90)
    val performanceDate = openingDay.plusDays(99)

    show.inventory(queryDate, performanceDate).map(_ shouldEqual Some(PerformanceInventory(show, 25, 5, TicketSaleState.OpenForSale)))
  }

}
