package com.github.scalaspring.akka

import akka.actor.ActorSystem
import org.springframework.beans.factory.annotation.Autowired

import scala.concurrent.ExecutionContextExecutor

trait AkkaAutowiredImplicits {

  @Autowired implicit val system: ActorSystem = null
  @Autowired(required = false) private val _executor: ExecutionContextExecutor = null

  // executor property that defaults to the actor system's dispatcher if no executor bean defined in the application context
  implicit def executor: ExecutionContextExecutor = {
    _executor match {
      case null => system.dispatcher
      case _ => _executor
    }
  }
}
