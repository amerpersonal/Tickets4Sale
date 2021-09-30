package tickets4sale.models

import org.joda.time.{Days, LocalDate}
import tickets4sale.config.Config


object TicketSaleState extends Enumeration with Config {
  type TicketSaleState = String

  val SaleNotStarted = "Sale not started"
  val OpenForSale = "Open for sale"
  val SoldOut = "Sold out"
  val InThePast = "In the past"


  def ticketState(queryDate: LocalDate, performanceDate: LocalDate): TicketSaleState = {
    val remainingDaysUntilPerformance = Days.daysBetween(queryDate, performanceDate).getDays + 1

    if (remainingDaysUntilPerformance > saleStartsBefore) TicketSaleState.SaleNotStarted
    else if (remainingDaysUntilPerformance > saleEndsBefore && remainingDaysUntilPerformance < saleStartsBefore) TicketSaleState.OpenForSale
    else if (remainingDaysUntilPerformance > 0 && remainingDaysUntilPerformance < saleEndsBefore) TicketSaleState.SoldOut
    else TicketSaleState.InThePast
  }

}
