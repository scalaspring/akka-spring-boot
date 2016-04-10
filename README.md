### Akka Spring Boot Integration (akka-spring-boot)

Scala-based integration of Akka with Spring Boot.
Two-way Akka<->Spring configuration bindings and convention over configuration with sensible automatic defaults get your project running quickly.

The goal of this project is to produce bootable, Scala-based Spring Boot applications with minimal configuration.

#### Key Benefits
1. Full Spring dependency injection support
   * Autowire any dependency into your actors and leverage the full Spring ecosystem where it makes sense
   * Use existing Spring components to enable gradual migration or reuse of suitable existing enterprise components
   * Avoid the downsides of using Scala implicits or abstract types to implement dependency injection. While both are excellent features, they can also lead to tight coupling and less maintainable code.
2. Configure Akka via any Spring property source
   * Use your Spring Boot configuration (YAML, properties files, or any property source) to set Akka properties. Any property set via Spring is visible via Akka Config.
   * Seamless two-way integration of Akka and Spring configuration - any property defined in Akka configuration is accessible via Spring and vice versa.
3. Pre-configured default actor system that's managed for you
   * No need to create and manage an actor system for your actors. A default actor system will be created when your application starts and terminated when your application is stopped.
4. Easy creation of actor beans and actor references
   * Simple, standard annotations and familiar actorOf() methods are all that's required to create actors that integrate with Spring.

#### Getting Started

##### build.sbt

````scala
libraryDependencies ++= "com.github.scalaspring" %% "akka-spring-boot" % "0.3.1"
````

##### Create an Actor and a Spring configuration

````scala
@ActorComponent
class EchoActor extends Actor {
  override def receive = {
    case message ⇒ sender() ! message
  }
}

@Configuration
@ComponentScan
@Import(Array(classOf[AkkaAutoConfiguration]))
class EchoConfiguration extends ActorSystemConfiguration {

  @Bean
  def echoActor = actorOf[EchoActor]

}
````

###### Notes on the code

* Actors
  * Annotate your actors with `@ActorComponent`, a [Spring meta-annotation](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/beans.html#beans-meta-annotations).
    This is simply a more readable way of marking your actors as Spring prototype beans.
* Configurations
  * Extend the `ActorSystemConfiguration` trait, which includes the helpful `actorOf()` methods
  * Import the `AkkaAutoConfiguration` configuration, which creates and manages the default actor system
  * Note that the `@ComponentScan` annotation will cause the EchoActor class to get picked up as a bean.
* Configuration Properties
  * Use the `akka.actorSystem.lifecycle.phase` configuration property to control when the underlying `ActorSystem` is terminated. The default value is -10 to ensure its termination after any default beans. 

##### Test the Configuration

Create a ScalaTest-based test that uses the configuration (see the [scalatest-spring](https://github.com/scalaspring/scalatest-spring) project)

````scala
@ContextConfiguration(
  loader = classOf[SpringApplicationContextLoader],
  classes = Array(classOf[EchoConfiguration])
)
class EchoConfigurationSpec extends FlatSpec with TestContextManagement with Matchers with AskSupport with ScalaFutures with StrictLogging {

  implicit val timeout: Timeout = (1 seconds)

  @Autowired val echoActor: ActorRef = null

  "Echo actor" should "receive and echo message" in {
    val message = "test message"
    val future = echoActor ? message

    whenReady(future) { result =>
      logger.info(s"""received result "$result"""")
      result should equal(message)
    }
  }

}
````

#### FAQ

##### How do I inject dependencies into my Scala classes?

###### Option 1: Constructor injection (Recommended)

Use the standard Spring `@Autowired` (or Java's `@Inject`) annotation on your class constructor(s). Note that the parentheses on the `@Autowired` annotation are required.

For example, assuming a bean of type MyService is defined in your configuration, the following actor will be injected with the appropriate dependency.
Note that this technique works with any Scala class, not just Actors. Use one of the standard Spring annotations (`@Component`, `@Service`, etc.) instead of `@ActorComponent`.

````scala
@Service
class SomeService {
  def someMethod() = { ... }
}

@ActorComponent
class SomeActor @Autowired() (val service: SomeService) extends Actor {
  override def receive = {
    // Call methods on service ...
  }
}

````

###### Option 2: Field injection

Use the standard Spring `@Autowired` (or Java's `@Inject`) annotation on class fields. Note that Spring will set read-only (val) fields.

````scala
@Component
class SomeComponent {
  def someMethod() = { ... }
}

@ActorComponent
class SomeActor extends Actor {

  @Autowired val component: SomeComponent = null

  override def receive = {
    // Call methods on component ...
  }
}

````

##### How is this project different than the spring-scala project from Pivotal Labs?

The two projects have different purposes and approaches.
The [scala-spring](https://github.com/spring-projects/spring-scala) project strives to make Spring accessible via functional configuration.
This project uses a different approach, relying on standard Spring annotations, and more tightly integrates with Akka.
Note that the scala-spring project is no longer maintained.
