package tickets4sale.config

import com.typesafe.config.ConfigFactory


trait Config {
  val config = ConfigFactory.load()

  val saleStartsBefore = config.getInt("tickets4sale.show.ticket-sale-start-before")
  val saleEndsBefore = config.getInt("tickets4sale.show.ticket-sale-end-before")
  val showDuration = config.getInt("tickets4sale.show.days-running")

}
