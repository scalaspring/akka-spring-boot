package com.github.scalaspring.akka

import akka.actor.ActorSystem
import org.springframework.beans.factory.annotation.Autowired

/**
 * Extend this trait to add actor reference creation helper methods to any Spring configuration.
 */
trait ActorSystemConfiguration extends SpringActorRefFactory {

  @Autowired
  protected implicit val factory: ActorSystem = null

}
