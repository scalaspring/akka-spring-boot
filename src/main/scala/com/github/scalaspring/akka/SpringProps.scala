package com.github.scalaspring.akka

import akka.actor._
import org.springframework.context.ConfigurableApplicationContext

import scala.reflect.ClassTag

/**
 * Adapter class to create standard Akka Props backed by Spring beans.
 * Retrieves application context from the implicit ActorRefFactory (either the actor system or an actor).
 *
 * NOTE: This class is typically NOT used directly, but rather through the {@code SpringActorRefFactory} trait.
 *
 * @see SpringActorRefFactory
 * @see ActorSystemConfiguration
 * @see SpringActor
 */
object SpringProps {

  def apply[T <: Actor: ClassTag](implicit factory: ActorRefFactory): Props = apply(implicitly[ClassTag[T]].runtimeClass)

  def apply[T <: Actor: ClassTag](args: Any*)(implicit factory: ActorRefFactory): Props = apply(implicitly[ClassTag[T]].runtimeClass, args: _*)

  def apply(clazz: Class[_], args: Any*)(implicit factory: ActorRefFactory): Props = Props(classOf[SpringIndirectActorProducer], clazz, getApplicationContext(factory), args.toList)

  def apply(beanName: String, args: Any*)(implicit factory: ActorRefFactory): Props = Props(classOf[SpringIndirectActorProducer], beanName, getApplicationContext(factory), args.toList)

  private def getApplicationContext(factory: ActorRefFactory): ConfigurableApplicationContext =
    Option(getActorSystem(factory).extension(SpringExtension).applicationContext).getOrElse(throw new IllegalStateException(s"Extension ${SpringExtension.getClass.getSimpleName} not initialized; application context is null"))

  private def getActorSystem(factory: ActorRefFactory): ActorSystem = factory match {
    case system: ActorSystem => system
    case context: ActorContext => context.system
    case _ => throw new IllegalArgumentException("unexpected ActorRefFactory type")
  }
}
