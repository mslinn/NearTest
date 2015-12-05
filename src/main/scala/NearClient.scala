import com.hazelcast.Scala._
import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import com.hazelcast.config.{InMemoryFormat, NearCacheConfig}
import com.hazelcast.config.EvictionPolicy.LRU
import com.hazelcast.core.IMap
import com.typesafe.config.ConfigFactory
import scala.collection.mutable

/** Read-mostly client, only updates user-related info */
object NearClient extends App {
  import Settings._

  val appConfig = ConfigFactory.parseResources("nearClient.conf")
  val citiesConfig = appConfig.getConfig(cacheName).resolve

  val clientConfig = new ClientConfig()
  var nearCacheConfig: NearCacheConfig =
    new NearCacheConfig(cacheName)
      .setCacheLocalEntries(true)
      .setInvalidateOnChange(true)
      .setMaxSize(citiesConfig.getInt("maxSize"))
      .setTimeToLiveSeconds(citiesConfig.getInt("ttl"))
      .setEvictionPolicy(LRU.name)
      .setInMemoryFormat(InMemoryFormat.OBJECT)
  val nearCache =  mutable.HashMap.empty[String, NearCacheConfig]
  clientConfig.addNearCacheConfig(nearCacheConfig)

  val client = HazelcastClient.newHazelcastClient(clientConfig)
  var cityProxy: IMap[Long, String] = client.getMap(cacheName)

  // FIXME onKeyEvent only fires if the modified cityCache.key==0
  cityProxy.onEntryEvents() {
    case EntryAdded(key, value) =>
      println(s"Key $key added with value $value")

    case EntryUpdated(key, oldValue, newValue) =>
      println(s"Key $key updated with value $newValue")

    case event =>
      println(s"Unhandled event $event")
  }
}
