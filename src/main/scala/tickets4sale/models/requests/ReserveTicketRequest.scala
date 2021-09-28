package tickets4sale.models.requests

import org.joda.time.LocalDate

final case class ReserveTicketRequest(title: String, performanceDate: LocalDate)

