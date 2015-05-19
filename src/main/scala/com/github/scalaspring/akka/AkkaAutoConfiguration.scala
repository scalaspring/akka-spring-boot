package com.github.scalaspring.akka

import akka.actor.{ActorSystem, Deploy, Props}
import com.github.scalaspring.akka.config.AkkaConfigAutoConfiguration
import com.typesafe.config.Config
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.{Bean, Configuration, Import}

import scala.concurrent.ExecutionContext

/**
 * Configures Akka for use in a Spring application using reasonable defaults.
 * Most beans in this configuration are conditional and will only be defined if not already present in the context.
 * This allows user-defined configuration to override defaults.
 *
 * This configuration provides the following:
 * 1. A default actor system based on the supplied Akka Config. A single actor system is presumed sufficient
 *    for most applications.
 * 2. A SmartLifecycle bean that shuts down the actor system when the application context is stopped. This matches
 *    the lifetime of the actor system to the containing application context.
 * 3. A baseline Deploy instance used as the starting point for actors created within the application context.
 *
 * @see ActorRefFactory
 */
@Configuration
@Import(Array(classOf[AkkaConfigAutoConfiguration]))
class AkkaAutoConfiguration(actorSystemName: String) extends SpringLogging {

  def this() = this("default")

  @Autowired
  val applicationContext: ConfigurableApplicationContext = null

  @Autowired(required = false)
  val executionContext: ExecutionContext = null

  /**
   * Create a default actor system if none defined.
   *
   * Define a bean of type {@code ExecutionContext} to customize the actor system's execution context. If no such bean
   * is defined, the default Akka execution context will be used, which should be sufficient for most applications.
   *
   * Note that dispatchers can be customized on a per-actor basis, which should be used to segment workloads, as needed,
   * while maintaining a single actor system.
   *
   * Note: The {@code ActorSystem} trait includes a shutdown method that Spring would normally detect and call when
   * the application context is closed. The empty string destroyMethod {@code @Bean} annotation attribute disables this
   * detection to allow for explicit management of the actor system lifecycle via an {@code ActorSystemLifecycle} bean.
   *
   */
  @Bean/*(destroyMethod = "")*/ @ConditionalOnMissingBean(Array(classOf[ActorSystem]))
  def actorSystem(config: Config): ActorSystem = {
    log.info(s"""Creating actor system "${actorSystemName}"""")
    ActorSystem(actorSystemName, Option(config), Option(applicationContext.getClassLoader), Option(executionContext))
  }

  /**
   * Create a post processor to automatically register Spring extension on actor system(s).
   */
  @Bean
  def actorSystemBeanPostProcessor = new ActorSystemBeanPostProcessor(applicationContext)
  
  /**
   * Create a lifecycle bean to shut down the actor system when the application context is stopped.
   */
  @Bean @ConditionalOnMissingBean(Array(classOf[ActorSystemLifecycle]))
  def actorSystemLifecycle(actorSystem: ActorSystem): ActorSystemLifecycle = {
    log.info(s"""Creating lifecycle for actor system "${actorSystem.name}"""")
    ActorSystemLifecycle(actorSystem)
  }

  /**
   * Create a default actor Deploy instance if none defined.
   *
   * Define a bean of type {@code Deploy} to customize the deployment information used when creating actors.
   *
   * Note that deployment customization is supported at the following points.
   * For example, customizations defined during reference creation override those from a Deploy instance defined in the
   * application context.
   * 1. Application Context - Define a Deploy bean that will be used as the starting point for all actors
   * 2. Actor Component - Specify annotation properties, typically to allocate actors to specific dispatchers
   * 3. Actor Reference - Specify creation parameters to customize specific actor instances
   */
  @Bean @ConditionalOnMissingBean(Array(classOf[Deploy]))
  def defaultDeploy = Props.defaultDeploy

}
