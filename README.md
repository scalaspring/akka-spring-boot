### Akka Spring Boot Integration (akka-spring-boot)

Easy-to-use Scala-friendly integration of Akka with Spring Boot.
Convention over configuration and sensible automatic defaults get your project running quickly.

#### Key Benefits
1. Full Spring dependency injection support
   * Autowire any dependency into your actors and leverage the full Spring ecosystem
   * Use existing Spring components to enable gradual migration or to reuse perfectly suitable existing enterprise components
   * Avoid the anti-pattern of using Scala implicits to implement dependency injection. Scala implicits are great, but they're often abused, IMHO, to pass dependencies all the way down the call stack and throughout an application resulting in tight coupling and less maintainable code.
2. Configure Akka via any Spring property source
   * Use your Spring Boot configuration (YAML, properties files, or any property source) to set Akka properties. Any property set via Spring is visible in Akka.
   * Seamless two-way integration of Akka configuration and Spring property sources - any property defined in Akka configuration is accessible via Spring and vice versa.
3. Pre-configured default actor system that's managed for you
   * No need to create and manage an actor system for your actors. A default actor system will be created when your application context starts and terminated when your application context is closed.
4. Easy creation of actor beans and actor references
   * Simple, standard annotations and familiar actorOf() methods are all that's required to create actors that integrate with Spring.

#### Getting Started

##### build.sbt

````scala
libraryDependencies ++= "com.github.scalaspring" %% "akka-spring-boot" % "0.1.0"
````

##### Create an actor

Annotate your actors with `@ActorComponent`, a [Spring meta-annotation](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/beans.html#beans-meta-annotations). This is simply a more readable way of marking your actors as Spring prototype beans.

````scala
@ActorComponent
class EchoActor extends Actor {
  override def receive = {
    case message ⇒ sender() ! message
  }
}
````

##### Create a Spring Configuration

Create a configuration class that

1. Extends the ActorSystemConfiguration trait
2. Imports the AkkaAutoConfiguration configuration

Note that the `@ComponentScan` annotation will cause the previously defined actor to get picked up as a bean.

````scala
@Configuration
@ComponentScan
@Import(Array(classOf[AkkaAutoConfiguration]))
class Configuration extends ActorSystemConfiguration {

  @Bean
  def echoActor = actorOf[EchoActor]

}
````

##### Testing Your Configuration

Create a ScalaTest-based test that uses the configuration (see the [scalatest-spring](https://github.com/scalaspring/scalatest-spring) project)

````scala
@ContextConfiguration(
  loader = classOf[SpringApplicationContextLoader],
  classes = Array(classOf[AkkaAutoConfigurationSpec.Configuration])
)
class AkkaAutoConfigurationSpec extends FlatSpec with TestContextManagement with Matchers with AskSupport with ScalaFutures with StrictLogging {

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