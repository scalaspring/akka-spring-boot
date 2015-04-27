package com.github.scalaspring.akka

import akka.actor.{Actor, ActorRef}
import akka.pattern.AskSupport
import akka.testkit.TestActors.EchoActor
import akka.util.Timeout
import com.github.scalaspring.akka.AkkaAutoConfigurationSpec.KeyValueProtocol
import com.github.scalaspring.scalatest.TestContextManagement
import com.typesafe.scalalogging.StrictLogging
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.context.annotation.{Bean, ComponentScan, Import}
import org.springframework.stereotype.Component
import org.springframework.test.context.ContextConfiguration

import scala.concurrent.duration._

@ContextConfiguration(
  loader = classOf[SpringApplicationContextLoader],
  classes = Array(classOf[AkkaAutoConfigurationSpec.Configuration])
)
class AkkaAutoConfigurationSpec extends FlatSpec with TestContextManagement with Matchers with AskSupport with ScalaFutures with StrictLogging {

  import KeyValueProtocol._

  import scala.concurrent.ExecutionContext.Implicits.global
  implicit val timeout: Timeout = (1 seconds)

  @Autowired val echoActor: ActorRef = null
  @Autowired val forwardingActor: ActorRef = null
  @Autowired val keyValueActor: ActorRef = null


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

  "Key value actor" should "put and get value by key" in {
    val (key, value) = ("someKey", "someValue")

    def checkResult(result: Any) = {
      result match {
        case Entry(k, v) => {
          logger.info(s"""received entry "$result"""")
          k shouldBe key
          v shouldBe Some(value)
        }
      }
    }

    // First put then get and verify same value retrieved
    val putFuture = keyValueActor ? Put(key, value)
    whenReady(putFuture)(checkResult _)

    val getFuture = putFuture.flatMap { case Entry(k, v) => keyValueActor ? Get(k) }
    whenReady(getFuture)(checkResult _)

  }

}


object AkkaAutoConfigurationSpec {

  @Configuration
  @ComponentScan
  @Import(Array(classOf[AkkaAutoConfiguration]))
  class Configuration extends ActorSystemConfiguration {

    @ActorBean
    def echoActorBean = new EchoActor()

    @Bean
    def echoActor = actorOf[EchoActor]

    @Bean
    def echoActor2 = actorOf[EchoActor]

    @Bean
    def forwardingActor(echoActor: ActorRef) = actorOf[ForwardingActor](echoActor)

    @Bean
    def parentActor = actorOf(SpringProps[ParentActor], "parent")

    @Bean
    def keyValueActor = actorOf[KeyValueActor]

  }

  /**
   * Demonstrates an actor with a constructor parameter.
   *
   * @param nextActor the forwarding destination for all received messages
   */
  @ActorComponent
  class ForwardingActor(nextActor: ActorRef) extends Actor with StrictLogging {
    override def receive = {
      case message => {
        logger.info(s"Forwarding message $message to $nextActor")
        nextActor forward message
      }
    }
  }

  /**
   * Demonstrates a parent/child actor.
   */
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

  @Component
  class KeyValueStore extends collection.mutable.HashMap[String, String] with StrictLogging

  object KeyValueProtocol {
    sealed trait Message
    case class Put(key: String, value: String) extends Message
    case class Get(key: String) extends Message
    case class Entry(key: String, value: Option[String]) extends Message
  }

  /**
   * Demonstrates constructor injection using Autowired annotation.
   */
  @ActorComponent
  class KeyValueActor @Autowired() (val store: KeyValueStore) extends Actor with StrictLogging {

    import KeyValueProtocol._

    logger.info("Starting up key value store actor...")

    override def receive = {
      case put: Put => {
        logger.info(s"Putting value into key value store ($put)")
        store.put(put.key, put.value)
        sender() ! Entry(put.key, Some(put.value))
      }
      case get: Get => {
        logger.info(s"Getting value from key value store ($get)")
        sender() ! Entry(get.key, store.get(get.key))
      }
      case _ => logger.info("Received unknown message")
    }
  }

}

