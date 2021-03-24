package learning

import akka.actor.{ActorSystem, Props}
import akka.serialization.Serializer
import com.typesafe.config.ConfigFactory
import spray.json._

/**
  * Messages and events are serialized with Java by default
  * (serialize = turn objects into bytes to send over the wire)
  * Java serialization sucks [slow, memory heavy, security vulnerabilities]
  * In this file: write our own serializers [custom format, JSON]
  */

case class Person(name: String, age: Int)

class PersonSerializer extends Serializer {

  val SEPARATOR = "//"

  override def identifier: Int = 3434

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case person@Person(name, age) =>
      println(s"Serializing $person")
      s"[$name$SEPARATOR$age]".getBytes // my own format, duh, like JSON
    case _ => throw new IllegalArgumentException("only persons are supported for this serializer")
  }

  override def includeManifest: Boolean = false

  override def fromBinary(bytes: Array[Byte], manifest: Option[Class[_]]): AnyRef = {
    val string = new String(bytes)
    val values = string.substring(1, string.length - 1).split(SEPARATOR)
    val person = Person(values(0), values(1).toInt)
    println(s"Deserialized $person")
    person
  }
}

class PersonJSONSerializer extends Serializer with DefaultJsonProtocol {

  implicit val personFormat: RootJsonFormat[Person] = jsonFormat2(Person) //Person is companion object, apply method called

  override def identifier: Int = 3223

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case person@Person(name, age) =>
      val json = person.toJson.prettyPrint
      println(s"Converting $person to $json")
      json.getBytes
    case _ => throw new IllegalArgumentException("only persons are supported for this serializer")
  }

  override def includeManifest: Boolean = false

  override def fromBinary(bytes: Array[Byte], manifest: Option[Class[_]]): AnyRef = {
    val str = new String(bytes)
    val person = str.parseJson.convertTo[Person]
    println(s"Deserialized $person")
    person
  }
}

object CustomSerialization_Local extends App {

  val config = ConfigFactory.parseString(
    """
      |akka.remote.artery.canonical.port = 2551
      |""".stripMargin
  ).withFallback(ConfigFactory.load("customSerialization.conf"))
  val system = ActorSystem("LocalSystem", config)
  val actorSelection = system.actorSelection("akka://RemoteSystem@localhost:2552/user/remoteActor")
  actorSelection ! Person("Alice", 22)
}

object CustomSerialization_Remote extends App {
  val config = ConfigFactory.parseString(
    """
      |akka.remote.artery.canonical.port = 2552
      |""".stripMargin
  ).withFallback(ConfigFactory.load("customSerialization.conf"))
  val system = ActorSystem("RemoteSystem", config)

  val simpleActor = system.actorOf(Props[SimpleActor], "remoteActor")
}

object CustomSerialization_Persistence extends App {

  val config = ConfigFactory.load("persistentStores.conf").getConfig("postgresStore")
    .withFallback(ConfigFactory.load("customSerialization.conf"))

  val system = ActorSystem("PersistenceSystem", config)

  val simplePersistentActor = system.actorOf(SimplePersistentActor.props("person-json"), "personJsonActor")

  simplePersistentActor ! Person("Aamir", 32)

}