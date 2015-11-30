import java.util.Properties

import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import com.hazelcast.config.NearCacheConfig.LocalUpdatePolicy
import com.hazelcast.config.{ListenerConfig, NearCacheConfig, NearCacheConfigReadOnly}
import com.hazelcast.core.{EntryEvent, IMap, MapEvent}
import com.hazelcast.map.listener.MapListener
import scala.collection.mutable

object MyEntryListener extends MapListener {
  def entryAdded(event: EntryEvent[Long, String]) =
    println("entryAdded:" + event)

  def entryRemoved(event: EntryEvent[Long, String]) =
    println("entryRemoved:" + event)

  def entryUpdated(event: EntryEvent[Long, String]) =
    println("entryUpdated:" + event)

  def entryEvicted(event: EntryEvent[Long, String]) =
    println("entryEvicted:" + event)

  def mapEvicted(event: MapEvent) =
    println("mapEvicted:" + event)

  def mapCleared(event: MapEvent) =
    println("mapCleared:" + event)
}

/** Read-mostly client, only updates user-related info */
object NearClient extends App {
  val clientConfig = new ClientConfig()
    .addListenerConfig(new ListenerConfig(MyEntryListener))
  var nearCacheConfig: NearCacheConfig =
    Option(clientConfig.getNearCacheConfig("Cities"))
      .getOrElse(new NearCacheConfig("Cities"))
        .setCacheLocalEntries(true)
        .setEvictionPolicy("LRU")
        .setMaxSize(Settings.mapSize)
        .setInvalidateOnChange(true)

  val nearCache =  mutable.HashMap.empty[String, NearCacheConfig]
  nearCache.put("CitiesLocal", nearCacheConfig)
  clientConfig.addNearCacheConfig(nearCacheConfig)

  // let's peek under the hood:
  val clientProps: Properties = clientConfig.getProperties
  val clientSer = clientConfig.getSerializationConfig
  val nearCacheConfigRO: NearCacheConfigReadOnly = nearCacheConfig.getAsReadOnly
  val localUpdatePolicy: LocalUpdatePolicy = nearCacheConfig.getLocalUpdatePolicy

  val client = HazelcastClient.newHazelcastClient(clientConfig)

  var cityProxy: IMap[Long, String] = client.getMap("Cities")
  println(s"NearClient: cityCache has ${cityProxy.size} elements")

  0 until cityProxy.size foreach { i => // IMPORTANT: index must be Long, not Int or null is returned
    println(s"NearClient: cityCache($i) = ${cityProxy.get(i.toLong)}")
  }

  val citiesLocal: IMap[Nothing, Nothing] = client.getMap("CitiesLocal")
  println(s"NearClient citiesLocal.size: ${citiesLocal.size}")
  0 until citiesLocal.size foreach { i =>
    println(s"NearClient: citiesLocal($i) = ${citiesLocal.get(i.toLong)}")
  }

  println("NearClient hits: " + cityProxy.getLocalMapStats.getNearCacheStats.getHits)
}
