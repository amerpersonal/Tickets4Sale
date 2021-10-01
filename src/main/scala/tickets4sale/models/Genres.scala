package tickets4sale.models

import scala.reflect.runtime.universe

object Genres {
  trait Genre {
    def name: String
    def price: Double
  }

  final object Musical extends Genre {
    val name = "MUSICAL"
    val price = 70
  }

  final object Comedy extends Genre {
    val name = "COMEDY"
    val price = 50
  }

  final object Drama extends Genre {
    val name = "DRAMA"
    val price = 40
  }

  val outer = universe.typeOf[Genres.type]
  val objects = outer.decls.filter(d => d.isModule).toList
  val mirror = universe.runtimeMirror(getClass.getClassLoader)

  def getGenres(): Seq[Genre] = {
    objects.collect {
      case o if(mirror.reflectModule(o.asModule).instance.isInstanceOf[Genre]) => mirror.reflectModule(o.asModule).instance.asInstanceOf[Genre]
    }
  }

  val genres = getGenres()

  def fromName(name: String): Option[Genre] = genres.find(_.name == name)


}
