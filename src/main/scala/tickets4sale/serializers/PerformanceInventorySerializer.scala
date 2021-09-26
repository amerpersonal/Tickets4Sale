package tickets4sale.serializers

import spray.json.DefaultJsonProtocol
import tickets4sale.models.PerformanceInventory

object PerformanceInventorySerializer extends DefaultJsonProtocol {
  import ShowSerializer._

  implicit val performanceInventorySerializer = jsonFormat4(PerformanceInventory)
}
