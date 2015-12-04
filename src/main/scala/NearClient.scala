import com.hazelcast.Scala._
import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import com.hazelcast.config.NearCacheConfig
import com.hazelcast.core.IMap
import scala.collection.mutable

/** Read-mostly client, only updates user-related info */
object NearClient extends App {
  val clientConfig = new ClientConfig()
  var nearCacheConfig: NearCacheConfig =
    Option(clientConfig.getNearCacheConfig(Settings.cacheName))
      .getOrElse(new NearCacheConfig(Settings.cacheName))
        .setCacheLocalEntries(true)
        .setEvictionPolicy("LRU")
        .setMaxSize(Settings.mapSize)
        .setInvalidateOnChange(true)
  val nearCache =  mutable.HashMap.empty[String, NearCacheConfig]
  clientConfig.addNearCacheConfig(nearCacheConfig)

  val client = HazelcastClient.newHazelcastClient(clientConfig)
  var cityProxy: IMap[Long, String] = client.getMap(Settings.cacheName)

  // FIXME onKeyEvent only fires if the modified cityCache.key==0
  cityProxy.onKeyEvents() {
    case KeyAdded(key) =>
      println(s"Key $key added with value ${cityProxy.get(key)}")

    case KeyUpdated(key) =>
      println(s"Key $key updated with value ${cityProxy.get(key)}")

    case event =>
      println(s"Unhandled event $event")
  }
}
