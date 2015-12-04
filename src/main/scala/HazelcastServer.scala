import collection.JavaConverters._
import collection.mutable
import com.hazelcast.config.Config
import com.hazelcast.core.{HazelcastInstance, Hazelcast}

object Settings {
  val mapSize = 10
  val prompt = "key,value > "
  val cacheName = "Cities"
}

/** Domain objects are stored as mutable Maps here, next to the DB */
object HazelcastServer extends App {
  val clientConfig = new Config
  val hcInstance: HazelcastInstance = Hazelcast.newHazelcastInstance(clientConfig)

  // Nils: Is there an idiomatic Scala way of writing this that does not require asInstanceOf?
  val cityCache = hcInstance.getMap(Settings.cacheName).asScala.asInstanceOf[mutable.Map[Long, String]]

  0 to Settings.mapSize foreach { i => cityCache.put(i.toLong, s"City #$i") }

  println("Enter a key,value pair for a cityCache entry (key value must >=0 and <$mapSize)")
  print(Settings.prompt)
  io.Source.stdin.getLines.foreach { line =>
    try {
      val Array(key, value) = line.split(",").map(_.trim)
      println(s"Updating cityCache($key) with '$value'")
      cityCache.put(key.toLong, value)
    } catch {
      case e: Exception =>
        println(e.getMessage)
    } finally {
      print(Settings.prompt)
    }
  }
}
