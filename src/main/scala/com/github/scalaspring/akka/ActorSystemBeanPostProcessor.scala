package com.github.scalaspring.akka

import akka.actor.ActorSystem
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.ConfigurableApplicationContext

/**
 * Registers {@code SpringExtension} on any actor system created in the application context.
 */
class ActorSystemBeanPostProcessor(applicationContext: ConfigurableApplicationContext) extends BeanPostProcessor with SpringLogging {

  override def postProcessBeforeInitialization(bean: AnyRef, beanName: String): AnyRef = {
    bean match {
      case system: ActorSystem => {
        log.info(s"Registering extension ${SpringExtension.getClass.getSimpleName} on actor system ${system.name}")
        system.registerExtension(SpringExtension).applicationContext = applicationContext
        bean
      }
      case _ => bean
    }
  }

  override def postProcessAfterInitialization(bean: AnyRef, beanName: String): AnyRef = bean

}
