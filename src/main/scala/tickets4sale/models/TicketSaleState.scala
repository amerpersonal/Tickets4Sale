package tickets4sale.models

object TicketSaleState extends Enumeration {
  type TicketSaleState = String

  val SaleNotStarted = "Sale not started"
  val OpenForSale = "Open for sale"
  val SoldOut = "Sold out"
  val InThePast = "In the past"

}
