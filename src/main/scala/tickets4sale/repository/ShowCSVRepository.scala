package tickets4sale.repository

import tickets4sale.models.Show

import scala.io.Source
import scala.util.Success

trait ShowCSVRepository extends ShowRepository {
  def loadShows(): Seq[Show] = {
    Source.fromFile("shows.csv").getLines().toSeq.map(Show.readFromLine(_)).collect { case Success(s) => s }
  }
}
