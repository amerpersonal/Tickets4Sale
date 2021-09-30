package tickets4sale.config

import com.typesafe.config.ConfigFactory


trait Config {
  val config = ConfigFactory.load()

  val saleStartsBefore = config.getInt("tickets4sale.show.ticket-sale-start-before")
  val saleEndsBefore = config.getInt("tickets4sale.show.ticket-sale-end-before")
  val showDuration = config.getInt("tickets4sale.show.days-running")

  val dbHost = config.getString("tickets4sale.database.host")
  val dbPort = config.getInt("tickets4sale.database.port")
  val dbName = config.getString("tickets4sale.database.dbname")

  val dbUsername = config.getString("tickets4sale.database.username")
  val dbPassword = config.getString("tickets4sale.database.password")

  val dbAppName = config.getString("tickets4sale.database.app-name")

  val dbUseSsl = config.getBoolean("tickets4sale.database.use-ssl")

  val serverHost = config.getString("akka.http.server.interface")
  val serverPort = config.getInt("akka.http.server.port")


}
