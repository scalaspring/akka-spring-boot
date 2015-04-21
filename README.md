# Akka Spring Boot Integration Library

Easy-to-use Scala-friendly integration of Akka with Spring Boot.
Get started quickly with minimal code by using a library that favors convention over configuration.

## Key Requirements
* Akka configured via Spring configuration (property sources)
* Supply a default actor system
* Match actor system lifecycle to Spring context
* Ease the creation of actor beans and actor references

## Getting Started

### build.sbt

````scala
libraryDependencies ++= "com.github.scalaspring" %% "akka-spring-boot" % "0.1.0"
````

### Create a Spring Configuration

Create a configuration class that extends the ActorSystemConfiguration trait and imports the AkkaAutoConfiguration

````scala
@Configuration
@ComponentScan
@Import(Array(classOf[AkkaAutoConfiguration]))
class Configuration extends ActorSystemConfiguration {

  // Note: the EchoActor class is part of Akka test kit
  @Bean
  def echoActor = actorOf[EchoActor]

}
````

### Testing Your Configuration

Create a ScalaTest-based test that uses the configuration

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