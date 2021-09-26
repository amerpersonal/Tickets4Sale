package tickets4sale.models

import spray.json._
import tickets4sale.models.TicketSaleState.TicketSaleState
import tickets4sale.serializers.PerformanceInventorySerializer._

case class PerformanceInventory(show: Show, ticketsLeft: Int, ticketsAvailable: Int, status: TicketSaleState) {
  override def toString: String = this.toJson.prettyPrint
}
