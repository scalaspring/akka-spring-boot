package com.github.lancearlaus.akka.spring

import java.beans.Introspector

import akka.actor._

import scala.reflect.ClassTag

/**
 * This trait should NOT be used directly. Please use the configuration and actor-specific extensions instead.
 */
trait SpringActorRefFactory {

  /**
   * The factory used to create actor references.
   * Note that this will either be the actor system itself or, in the case of child actors, the parent actor's context.
   */
  protected implicit val factory: ActorRefFactory

  /**
   * Creates a Spring-backed actor reference by type.
   *
   * This factory method satisfies most use cases and is appropriate when a single actor of a given type is defined.
   *
   * Example:
   *
   * {{{
   *   @Bean
   *   def myActor = actorOf[MyActor]
   * }}}
   *
   */
  def actorOf[T <: Actor: ClassTag]: ActorRef = requireFactory(factory.actorOf(SpringProps[T]))

  /**
   * Creates a customized Spring-backed actor reference by type.
   *
   * This factory method is typically used when the target actor needs to be customized per consumer during configuration.
   *
   * Example:
   *
   * {{{
   *    val someString = "some string"
   *
   *    @Bean
   *    def myActor = actorOf[MyActor](someString)
   * }}}
   *
   */
  def actorOf[T <: Actor: ClassTag](args: Any*): ActorRef = requireFactory(factory.actorOf(SpringProps[T](args: _*)))

  /**
   * Creates a Spring-backed actor reference by name with optional constructor arguments.
   *
   * This factory method is typically used to disambiguate in the case of multiple beans defined for the same type. If
   * this is not the case, consider using the preferred type-based reference factory methods.
   *
   * Example:
   *
   * {{{
   *    @Bean
   *    def myActor = actorOf("myActorBean2")
   * }}}
   *
   * @param beanName name of the underlying bean
   * @param args optional constructor arguments
   */
  def actorOf(beanName: String, args: Any*): ActorRef = requireFactory(factory.actorOf(SpringProps(beanName, args: _*)))

  def actorOf(props: Props): ActorRef = requireFactory(factory.actorOf(props))

  def actorOf(props: Props, name: String): ActorRef = requireFactory(factory.actorOf(props, name))


  private def requireFactory(f: => ActorRef) = {
    require(factory != null, "You cannot create actors from within a configuration constructor (using val or var). " +
      s"""Define a bean factory method annotated with the @Bean annotation instead. See the documentation for the ${classOf[SpringActorRefFactory].getSimpleName}.actorOf methods.""")
    f
  }

  protected def generateActorName[T <: Actor: ClassTag] =
    Introspector.decapitalize(implicitly[ClassTag[T]].runtimeClass.getSimpleName)

}
