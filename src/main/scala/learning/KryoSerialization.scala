package learning

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import learning.{Person, SimpleActor, SimplePersistentActor}

/**
  * Popular serialization framework for JVM.
  * Its super fast, memory-efficient, highly configurable for akka
  * can serialize almost anything out of the box
  * poorly maintained since Akka 2.4
  */

case class Book(title: String, year: Int)
object KryoSerialization_Local extends App {

  val config: Any = ConfigFactory.parseString(
    """
      |akka.remote.artery.canonical.port = 2551
      |""".stripMargin
  ).withFallback(ConfigFactory.load("kryoSerialization.conf"))
 // val system = ActorSystem("LocalSystem", config)
//  val actorSelection = system.actorSelection("akka://RemoteSystem@localhost:2552/user/remoteActor")
 // actorSelection ! Book("The Monk who sold his ferrari", 1996)
}

/**
  * Object graph complete in log, here for both remote and local, means object was completely serialized and sent over the wire
  */
object KryoSerialization_Remote extends App {
  val config = ConfigFactory.parseString(
    """
      |akka.remote.artery.canonical.port = 2552
      |""".stripMargin
  ).withFallback(ConfigFactory.load("kryoSerialization.conf"))
  val system = ActorSystem("RemoteSystem", config)

  val simpleActor = system.actorOf(Props[SimpleActor], "remoteActor")
}

object KryoSerialization_Persistence extends App {

  val config = ConfigFactory.load("persistentStores.conf").getConfig("postgresStore")
    .withFallback(ConfigFactory.load("kryoSerialization.conf"))

  val system = ActorSystem("PersistenceSystem", config)

  val simplePersistentActor = system.actorOf(SimplePersistentActor.props("kryo-actor"), "kryoBookActor")

  simplePersistentActor ! Book("The Monk who sold his ferrari", 1996)

}

/**
  *     use-manifests = false
      mappings {

        }
        classes = [

        ]

  // the above config will fail deserialization

   #post-serialization-transformations = "lz4,aes"
    comment out this part to make Hex Decode text as readable
  */