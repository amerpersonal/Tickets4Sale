package tickets4sale.database

import slick.jdbc.PostgresProfile.api._
import tickets4sale.config.Config

object DatabaseConnection extends Config {

  val connection = Database.forURL(dbUrl, dbUsername, dbPassword, null, "org.postgresql.Driver")


  def dbUrl(): String = {
    val sslOptions = if(dbUseSsl) s"&ssl=${dbUseSsl}&sslfactory=org.postgresql.ssl.NonValidatingFactory&sslmode=prefer" else ""

    val connectionString = s"jdbc:postgresql://${dbHost}:${dbPort}/${dbName}?ApplicationName=${dbAppName}${sslOptions}"

    connectionString
  }
}
