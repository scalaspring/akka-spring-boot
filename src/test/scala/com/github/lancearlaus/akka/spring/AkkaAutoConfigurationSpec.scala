package com.github.lancearlaus.akka.spring

import akka.actor.{Actor, ActorRef}
import akka.pattern.AskSupport
import akka.testkit.TestActors.EchoActor
import akka.util.Timeout
import com.github.scalaspring.spring.test.TestContextManagement
import com.typesafe.scalalogging.StrictLogging
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.context.annotation.{Bean, ComponentScan, Import}
import org.springframework.test.context.ContextConfiguration

import scala.concurrent.duration._

@ContextConfiguration(
  loader = classOf[SpringApplicationContextLoader],
  classes = Array(classOf[AkkaAutoConfigurationSpec.Configuration])
)
class AkkaAutoConfigurationSpec extends FlatSpec with TestContextManagement with Matchers with AskSupport with ScalaFutures with StrictLogging {

  implicit val timeout: Timeout = (1 seconds)

  @Autowired val echoActor: ActorRef = null
  @Autowired val forwardingActor: ActorRef = null


  "Echo actor" should "receive and echo message" in {
    val message = "test message"
    val future = echoActor ? message

    whenReady(future) { result =>
      logger.info(s"""received result "$result"""")
      result should equal(message)
    }
  }

  "Forwarding actor" should "receive and forward message" in {
    val message = "test message"
    val future = forwardingActor ? message

    whenReady(future) { result =>
      logger.info(s"""received result "$result"""")
      result should equal(message)
    }
  }

}


object AkkaAutoConfigurationSpec {

  @Configuration
  @ComponentScan
  @Import(Array(classOf[AkkaAutoConfiguration]))
  class Configuration extends ActorSystemConfiguration {

    @ActorBean
    def echoActorComponent = new EchoActor()

    @Bean
    def echoActor = actorOf[EchoActor]

    @Bean
    def echoActor2 = actorOf[EchoActor]

    @Bean
    def forwardingActor(echoActor: ActorRef) = actorOf[ForwardingActor](echoActor)

    @Bean
    def parentActor = actorOf(SpringProps[ParentActor], "parent")

  }

  @ActorComponent
  class ForwardingActor @Autowired() (nextActor: ActorRef) extends Actor with StrictLogging {
    override def receive = {
      case message => {
        logger.info(s"Forwarding message $message to $nextActor")
        nextActor forward message
      }
    }
  }

  @ActorComponent
  class ParentActor extends SpringActor with StrictLogging {

    val child = actorOf[EchoActor]

    logger.info(s"parent actor path: ${self.path}")
    logger.info(s"child actor path: ${child.path}")

    override def receive = {
      case message => {
        logger.info(s"Forwarding message $message to child")
        child forward message
      }
    }
  }

}

