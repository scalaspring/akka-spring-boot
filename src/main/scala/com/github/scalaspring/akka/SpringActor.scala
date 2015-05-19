package com.github.scalaspring.akka

import akka.actor.{Actor, ActorContext}

/**
 * Extend this trait to add factory helper methods to actors.
 */
trait SpringActor extends Actor with SpringActorRefFactory { this: Actor =>

  protected implicit val factory: ActorContext = context

}
