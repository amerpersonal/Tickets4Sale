package tickets4sale.services

import tickets4sale.config.Config
import tickets4sale.models.Show
import tickets4sale.repository.ShowRepository

trait ShowsService extends Config { this: ShowRepository =>

  def shows(): Seq[Show] = loadShows()

}
