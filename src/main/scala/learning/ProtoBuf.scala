/**
  * ProtoBuf --> Protocol Buffers
  * It's a serialization framework developed by Google.
  * It is very fast and very memory-efficient.
  * It also combines the benefit of "schema-based" via proto files (IDL)
  * i.e It combines benefit of schema generation and validation through [Interface Definition Language] proto file.
  * Best use case is "Schema Evolution" (overcomes schema adaptation layer addition as seen in akka persistence)
  * Akka already implemented Akka Protobuf serializer.
  * ProtoBuf is not without its limitations: it needs a dedicated code generator to translate these proto files from that
     IDL into some actual code that we can use,
  * Sadly, it doesn't support Scala out of the box, it does support java. But Scala can easily interoperate with Java
  * For running protoc file and generating Java code:
  * --> Download  translator/compiler whatever first for your OS: https://github.com/protocolbuffers/protobuf/releases and install protoc
  * --> create .proto file with our data structure.
  * --> under src folder run: ./main/exec/protoc --java_out=main/java main/proto/datamodel.proto to generate java file.
  * --> set the protobuf serializer in the .conf file for scala code.
  * --> In scala code we refer to the generated Java type, use the builder pattern to instantiate objects.
  *
  * Protobuf stores identifiers i.e 1,2 etc instead of field names, i.e protobuf doesn't need to know the name of the field so
    helps in schema evolution.
    --> Either changing a field, e.g from userId to id or adding a new field (optional) works fine with protobuf
  */


package learning

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import learning.Datamodel.OnlineStoreUser


object ProtoBufSerialization_Local extends App {

  val config = ConfigFactory.parseString(
    """
      |akka.remote.artery.canonical.port = 2551
      |""".stripMargin
  ).withFallback(ConfigFactory.load("protobufSerialization.conf"))
  val system = ActorSystem("LocalSystem", config)
  val actorSelection = system.actorSelection("akka://RemoteSystem@localhost:2552/user/remoteActor")

  val onlineStoreUser: OnlineStoreUser = OnlineStoreUser.newBuilder()
    //.setUserId(45622) now changed it to id
    .setId(2121)
    .setUserName("aamir")
    .setUserEmail("aamir@gmail.com")
    .build() //not mentioning user phone as its optional

  actorSelection ! onlineStoreUser

}

object ProtoBufSerialization_Remote extends App {
  val config = ConfigFactory.parseString(
    """
      |akka.remote.artery.canonical.port = 2552
      |""".stripMargin
  ).withFallback(ConfigFactory.load("protobufSerialization.conf"))
  val system = ActorSystem("RemoteSystem", config)

  val simpleActor = system.actorOf(Props[SimpleActor], "remoteActor")
}

object ProtoBufSerialization_Persistence extends App {

  val config = ConfigFactory.load("persistentStores.conf").getConfig("postgresStore")
    .withFallback(ConfigFactory.load("protobufSerialization.conf"))

  val system = ActorSystem("PersistenceSystem", config)

  val simplePersistentActor = system.actorOf(SimplePersistentActor.props("protobuf-actor"), "protobufActor")

  val onlineStoreUser: OnlineStoreUser = OnlineStoreUser.newBuilder()
    //.setUserId(45622) //now id
    .setId(1212)
    .setUserName("aamir")
    .setUserEmail("aamir@gmail.com")
    .build() // even if userPhone is not mentioned, I can fetch records from recover as its optional.

  val otherRecord = OnlineStoreUser.newBuilder()
    .setId(111)
    .setUserName("shehzal")
    .setUserEmail("shehzal@email.com")
    .setUserPhone("21212121")
    .build()
  //simplePersistentActor ! otherRecord
}