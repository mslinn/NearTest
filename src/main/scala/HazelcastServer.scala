import collection.JavaConverters._
import collection.mutable
import com.hazelcast.config.Config
import com.hazelcast.core.{IMap, HazelcastInstance, Hazelcast}
import com.hazelcast.Scala._

object Settings {
  val mapSize = 10
  val prompt = "key,value > "
  val cacheName = "Cities"
}

/** Domain objects are stored as mutable Maps here, next to the DB */
object HazelcastServer extends App {
  import Settings._

  val clientConfig = new Config
  val hz: HazelcastInstance = Hazelcast.newHazelcastInstance(clientConfig)
  val cityCache: IMap[Long, String] = hz.getMap[Long, String](cacheName)
  0 to mapSize foreach { i => cityCache.put(i.toLong, s"City #$i") }

  println(s"Enter a key,value pair for a cityCache entry (key value must >=0 and <${cityCache.size})")
  print(prompt)
  io.Source.stdin.getLines.foreach { line =>
    try {
      val Array(key, value) = line.split(",").map(_.trim)
      println(s"Updating cityCache($key) with '$value'")
      cityCache.put(key.toLong, value)
    } catch {
      case e: Exception =>
        println(e.getMessage)
    } finally {
      print(prompt)
    }
  }
}
