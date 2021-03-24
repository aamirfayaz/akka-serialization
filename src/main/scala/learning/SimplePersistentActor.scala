package learning

import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentActor

class SimplePersistentActor(val persistenceId: String, shouldLog: Boolean) extends PersistentActor with ActorLogging {
  override def receiveRecover: Receive = {
    case event =>
      if(shouldLog)
        log.info(s"Recovered: $event")
  }

  override def receiveCommand: Receive = {
    case message => persist(message) { _ =>
      if(shouldLog)
        log.info(s"Persisted $message")

    }
  }

}

object SimplePersistentActor {
  def props(persistenceId: String, shouldLog: Boolean = true) =
    Props(new SimplePersistentActor(persistenceId, shouldLog))
}