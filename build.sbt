organization := "com.micronautics"

name := "near-test"

version := "0.1.0"

scalaVersion := "2.11.7"

scalacOptions ++= Seq("-deprecation", "-encoding", "UTF-8", "-feature", "-target:jvm-1.7", "-unchecked",
    "-Ywarn-adapted-args", "-Ywarn-value-discard", "-Xlint")

scalacOptions in (Compile, doc) <++= baseDirectory.map {
  (bd: File) => Seq[String](
     "-sourcepath", bd.getAbsolutePath,
     "-doc-source-url", "https://github.com/mslinn/nearTest/tree/master€{FILE_PATH}.scala"
  )
}

javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked", "-source", "1.7", "-target", "1.7", "-g:vars")

resolvers ++= Seq(
  "nilskp/maven on bintray" at "http://dl.bintray.com/nilskp/maven",
  "Typesafe Releases"   at "http://repo.typesafe.com/typesafe/releases"
)

libraryDependencies ++= Seq(
  "com.hazelcast" %% "hazelcast-scala"  % "latest.integration" withSources(),
  "com.hazelcast" %  "hazelcast-client" % "3.5.4" withSources(),
  "com.typesafe"  %  "config"           % "1.3.0" withSources()
  //"org.scalatest"           %% "scalatest"     % "2.2.3" % "test" withSources(),
  //"com.github.nscala-time"  %% "nscala-time"   % "1.8.0" withSources()
)

logLevel := Level.Warn

// Only show warnings and errors on the screen for compilations.
// This applies to both test:compile and compile and is Info by default
logLevel in compile := Level.Warn

// Level.INFO is needed to see detailed output when running tests
logLevel in test := Level.Info

// define the statements initially evaluated when entering 'console', 'console-quick', but not 'console-project'
initialCommands in console := """
                                |import com.hazelcast.core.Hazelcast
                                |import com.hazelcast.core.HazelcastInstance
                                |import com.hazelcast.Scala._
                                |import com.hazelcast.client.HazelcastClient
                                |import com.hazelcast.client.config.ClientConfig
                                |import com.hazelcast.config.NearCacheConfig
                                |import com.hazelcast.core.IMap
                                |import scala.collection.mutable
                                |""".stripMargin

cancelable := true

sublimeTransitive := true
