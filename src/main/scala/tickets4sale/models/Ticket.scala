package tickets4sale.models

import org.joda.time.LocalDate

case class Ticket(show: Show, reservcationDate: LocalDate, performanceDate: LocalDate)
