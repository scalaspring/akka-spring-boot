package com.github.scalaspring.akka

import akka.actor.ActorSystem
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.SmartLifecycle


object ActorSystemLifecycle {
  def apply(actorSystem: ActorSystem) = new ActorSystemLifecycle(actorSystem)
}

/**
 * Shuts down the actor system when the application context is stopped.
 *
 * The lifecycle phase (default -10) can be adjusted by setting the akka.actorSystem.lifecycle.phase configuration
 * property. Note that the phase MUST be less than any beans that depend on the actor system to ensure that the
 * actor system is shut down after any dependent beans.
 */
class ActorSystemLifecycle(actorSystem: ActorSystem) extends SmartLifecycle with SpringLogging {

  override def isAutoStartup: Boolean = true

  @Value("${akka.actorSystem.lifecycle.phase:-10}")
  protected val phase: Int = -10
  override def getPhase: Int = phase

  override def isRunning: Boolean = !actorSystem.whenTerminated.isCompleted

  // Do nothing since the actor system is already started once created
  override def start() = {}

  override def stop(callback: Runnable): Unit = {
    if (actorSystem.whenTerminated.isCompleted) {
      log.warn(s"Actor system ${actorSystem.name} already terminated")
      callback.run()
    } else {
      log.info(s"Terminating actor system ${actorSystem.name}")
      actorSystem.registerOnTermination(callback)
      actorSystem.registerOnTermination { log.info(s"Actor system ${actorSystem.name} terminated") }
      actorSystem.terminate()
    }
  }

  // Leave this method not implemented since it shouldn't be called (the async stop() method should be called instead)
  final override def stop(): Unit = ???

}
