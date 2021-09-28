package tickets4sale

import org.flywaydb.core.Flyway
import tickets4sale.config.Config

object DatabaseMigrationRunner extends App with Config {
  override def main(args: Array[String]): Unit = {
    try {
      val flyway = new Flyway()
      flyway.setBaselineOnMigrate(false)
      flyway.setSchemas("public")
      flyway.setMixed(false)

      flyway.setDataSource(s"jdbc:postgresql://${dbHost}:${dbPort}/${dbName}", dbUsername, dbPassword)

      flyway.setLocations(s"db/migrations")

      flyway.migrate()

      System.exit(0)
    }
    catch {
      case ex: Exception =>
        println(s"Error when running DB migrations: ${ex.getMessage}")
        System.exit(1)
    }
  }
}
