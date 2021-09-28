package tickets4sale.repository

import tickets4sale.models.Show

trait ShowRepository {
  def loadShows(): Seq[Show]
}
