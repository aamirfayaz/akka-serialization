package learning

import com.sksamuel.avro4s.{AvroInputStream, AvroOutputStream, AvroSchema}
import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import akka.serialization.Serializer

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

/**
  * fast serializer but not as fast as kryo, but it has some other benefits.
  * Distinctive benefit is its Schema-based, i.e it can validate objects against a know schema.
  * one-line schema generation.
  * well-maintained library for scala.
  * good interoperability with Hadoop or Spark.
  * */


case class BankAccount(iban: String, bankCode: String, amount: Double, currency: String)

case class CompanyRegistry(name: String, accounts: Seq[BankAccount], activityCode: String, marketCap: Double)

class TheAvroSerializer extends Serializer {

  val companyRegistrySchema = AvroSchema[CompanyRegistry]

  override def identifier: Int = 42622

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case c: CompanyRegistry =>
      //to serialize this object, I need a schema to serialize it with

      val byteArrayOutputStream: ByteArrayOutputStream = new ByteArrayOutputStream()
      val avroOutputStream: AvroOutputStream[CompanyRegistry] = AvroOutputStream.binary[CompanyRegistry].to(byteArrayOutputStream).build(companyRegistrySchema)
      avroOutputStream.write(c)
      avroOutputStream.flush()
      avroOutputStream.close()
      byteArrayOutputStream.toByteArray

    case _ => throw new IllegalArgumentException("We only support company registries for Avro")
  }

  override def fromBinary(bytes: Array[Byte], manifest: Option[Class[_]]): AnyRef = {
    val avroInputStream: AvroInputStream[CompanyRegistry] = AvroInputStream.binary[CompanyRegistry].from(new ByteArrayInputStream(bytes)).build(companyRegistrySchema)
    val companyRegistryIterator: Iterator[CompanyRegistry] = avroInputStream.iterator

    val companyRegistry: CompanyRegistry = companyRegistryIterator.next()
    avroInputStream.close()
    companyRegistry
  }

  override def includeManifest: Boolean = false

}


object AvroSerialization_Local extends App {

  val config = ConfigFactory.parseString(
    """
      |akka.remote.artery.canonical.port = 2551
      |""".stripMargin
  ).withFallback(ConfigFactory.load("avroSerialization.conf"))
  val system = ActorSystem("LocalSystem", config)
  val actorSelection = system.actorSelection("akka://RemoteSystem@localhost:2552/user/remoteActor")

  val companyRegistry = CompanyRegistry("shehzal-corp",
    Seq(
      BankAccount("india-2345", "shehzal-bank", 2.4, "trillion rupees"),
      BankAccount("germany-2323", "aamir-bank", 4.2, "billion dollars")
    ),
   "IBIB-22", 202000.2
  )
  actorSelection ! companyRegistry
}

object AvroSSerialization_Remote extends App {
  val config = ConfigFactory.parseString(
    """
      |akka.remote.artery.canonical.port = 2552
      |""".stripMargin
  ).withFallback(ConfigFactory.load("avroSerialization.conf"))
  val system = ActorSystem("RemoteSystem", config)

  val simpleActor = system.actorOf(Props[SimpleActor], "remoteActor")
}

object AvroSerialization_Persistence extends App {

  val config = ConfigFactory.load("persistentStores.conf").getConfig("postgresStore")
    .withFallback(ConfigFactory.load("avroSerialization.conf"))

  val system = ActorSystem("PersistenceSystem", config)

  val simplePersistentActor = system.actorOf(SimplePersistentActor.props("avro-actor"), "avroActor")

  val companyRegistry = CompanyRegistry("shehzal-corp",
    Seq(
      BankAccount("india-2345", "shehzal-bank", 2.4, "trillion rupees"),
      BankAccount("germany-2323", "aamir-bank", 4.2, "billion dollars")
    ),
    "IBIB-22", 202000.2
  )
  simplePersistentActor ! companyRegistry
}

object SimpleAvroApp extends App {
  //create schema for this company
  println(AvroSchema[CompanyRegistry])
}