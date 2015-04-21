package com.github.lancearlaus.akka.spring

import akka.actor.ActorSystem
import org.springframework.context.SmartLifecycle


object ActorSystemLifecycle {
  def apply(actorSystem: ActorSystem) = new ActorSystemLifecycle(actorSystem)
}

/**
 * Shuts down the actor system when the application context is stopped.
 */
class ActorSystemLifecycle(actorSystem: ActorSystem) extends SmartLifecycle with SpringLogging {

  override def isAutoStartup: Boolean = true

  override def getPhase: Int = 0

  override def isRunning: Boolean = !actorSystem.isTerminated

  // Do nothing since the actor system is already started once created
  override def start(): Unit = { log.info(s"Starting actor system ${actorSystem.name}")}

  override def stop(callback: Runnable): Unit = {
    if (!actorSystem.isTerminated) {
      log.info(s"Shutting down actor system ${actorSystem.name}")
      actorSystem.registerOnTermination({ log.info(s"Shut down complete for actor system ${actorSystem.name}"); callback.run })
      actorSystem.shutdown()
    } else {
      log.warn(s"Actor system ${actorSystem.name} already terminated")
    }
  }

  // Leave this method not implemented since it shouldn't be called (the async stop() method should be called instead)
  final override def stop(): Unit = ???

}
