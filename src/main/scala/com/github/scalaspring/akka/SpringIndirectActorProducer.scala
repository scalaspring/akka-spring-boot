package com.github.scalaspring.akka

import akka.actor.{Actor, IndirectActorProducer}
import org.springframework.context.ConfigurableApplicationContext

import scala.collection.immutable

object SpringIndirectActorProducer {

  def getBeanNameForType(applicationContext: ConfigurableApplicationContext, clazz: Class[_]): String = {
    val beanNames = applicationContext.getBeanNamesForType(clazz)
    if (beanNames.length > 1) throw new IllegalArgumentException(s"Multiple beans found for actor class ${clazz.getName} (${beanNames}}). Please use name-based constructor to specify bean name to use.")
    beanNames.headOption.orElse(throw new IllegalArgumentException(s"No bean defined for actor class ${clazz.getName}")).get
  }

  def getTypeForBeanName(applicationContext: ConfigurableApplicationContext, beanName: String): Class[_ <: Actor] = {
    applicationContext.getBeanFactory.getType(beanName).asInstanceOf[Class[Actor]]
  }
}

import SpringIndirectActorProducer._

class SpringIndirectActorProducer(clazz: Class[_ <: Actor], applicationContext: ConfigurableApplicationContext, beanName: String,  args: immutable.Seq[AnyRef]) extends IndirectActorProducer {

  def this(clazz: Class[_ <: Actor], applicationContext: ConfigurableApplicationContext, args: immutable.Seq[AnyRef]) =
    this(clazz, applicationContext, getBeanNameForType(applicationContext, clazz), args)

  def this(beanName: String, applicationContext: ConfigurableApplicationContext, args: immutable.Seq[AnyRef]) =
    this(getTypeForBeanName(applicationContext, beanName), applicationContext, beanName, args)

  validateActorBeanDefinition

  protected def validateActorBeanDefinition: Unit = {
    val beanClass = applicationContext.getBeanFactory.getType(beanName)
    val beanDefinition = applicationContext.getBeanFactory.getBeanDefinition(beanName)

    require(actorClass.isAssignableFrom(beanClass), s"""Invalid bean type. Bean "${beanName}" of type ${beanClass.getSimpleName} does not extend ${actorClass.getSimpleName}.""")
    require(!beanDefinition.isSingleton, s"""Actor beans must be non-singleton. Suggested fix: Annotate ${beanDefinition.getBeanClassName} with the @${classOf[ActorComponent].getSimpleName} annotation to create actor beans with prototype scope.""")
    // TODO: Validate actor constructor if arguments supplied to enable fail fast (see akka.util.Reflect.findConstructor)
  }

  override def actorClass: Class[_ <: Actor] = clazz

  override def produce(): Actor = {
    args match {
      case s if s.isEmpty => applicationContext.getBean(beanName).asInstanceOf[Actor]
      case _ => applicationContext.getBean(beanName, args: _*).asInstanceOf[Actor]
    }
  }

}
