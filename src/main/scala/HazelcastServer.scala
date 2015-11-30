import collection.JavaConverters._
import collection.mutable
import com.hazelcast.config.Config
import com.hazelcast.core.{HazelcastInstance, Hazelcast}

object Settings {
  val mapSize = 10
  val prompt = "> "
}

/** Domain objects are stored as mutable Maps here, next to the DB */
object HazelcastServer extends App {
  val clientConfig = new Config
  val hcInstance: HazelcastInstance = Hazelcast.newHazelcastInstance(clientConfig)
  println(s"hcInstance=$hcInstance")

  val cityCache = hcInstance.getMap("Cities").asScala.asInstanceOf[mutable.Map[Long, String]]

  0 to Settings.mapSize foreach { i => cityCache.put(i, s"Vienna$i") }
  println("HazelcastServer: cityCache.size=" + cityCache.size)

  val citiesMap2 = hcInstance.getMap("Cities").asScala.asInstanceOf[mutable.Map[Long, String]]
  0 to Settings.mapSize foreach { i =>
    println(s"HazelcastServer: citiesMap2($i) = ${citiesMap2.get(i)}")
  }

  println("Enter new values for cityCache(0)")
  print(Settings.prompt)
  io.Source.stdin.getLines.foreach { line =>
    cityCache.put(0L, line)
    print(Settings.prompt)
  }
}
