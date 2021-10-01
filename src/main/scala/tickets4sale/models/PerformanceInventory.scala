package tickets4sale.models

import spray.json._
import tickets4sale.models.TicketSaleStates.TicketSaleState

case class PerformanceInventory(show: Show, ticketsLeft: Int, ticketsAvailable: Int, status: TicketSaleState) {
  import tickets4sale.serializers.PerformanceInventorySerializer._

  override def toString: String = this.toJson.prettyPrint
}
