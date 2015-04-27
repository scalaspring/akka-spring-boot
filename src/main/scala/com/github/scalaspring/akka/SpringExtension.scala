package com.github.scalaspring.akka

import akka.actor.{ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import org.springframework.context.ConfigurableApplicationContext

object SpringExtension extends ExtensionId[SpringExtension] with ExtensionIdProvider {

  override def lookup(): ExtensionId[_ <: Extension] = SpringExtension

  override def createExtension(system: ExtendedActorSystem): SpringExtension = new SpringExtension(system)

}

/**
 * Extension that holds the Spring application context.
 *
 * The applicationContext property is a write-once property that is set when creating the actor system bean
 * (via a bean post processor).
 * 
 * @see ActorSystemBeanPostProcessor
 */
class SpringExtension(system: ExtendedActorSystem) extends Extension {

  private var _applicationContext: ConfigurableApplicationContext = null

  def applicationContext = _applicationContext

  def applicationContext_=(applicationContext: ConfigurableApplicationContext) {
    require(applicationContext != null, "applicationContext must not be null")
    if (_applicationContext != null) throw new IllegalStateException("application context already set")
    _applicationContext = applicationContext
  }

}
