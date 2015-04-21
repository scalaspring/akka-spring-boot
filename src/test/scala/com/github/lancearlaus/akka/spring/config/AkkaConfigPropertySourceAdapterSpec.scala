package com.github.lancearlaus.akka.spring.config

import java.util

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.JavaConverters._

class AkkaConfigPropertySourceAdapterSpec extends FlatSpec with Matchers with StrictLogging {

  val goodProperties = textToProperties(
    """|list[0]=zero
       |list[1]=one
       |list[2]=two
       |normal=normal""".stripMargin
  )

  val badProperties = textToProperties(
    """|list[0]=zero
      |list[1]=one
      |list[2]=two
      |list=bad""".stripMargin
  )

  def textToProperties(text: String): java.util.Map[String, String] = {
    text.lines.map { line =>
      line.split('=') match {
        case Array(k, v) => (k, v)
        case _ => sys.error(s"invalid property format $line")
      }
    }.foldLeft(new java.util.LinkedHashMap[String, String]())((m, t) => { m.put(t._1, t._2); m })
  }

  def validateListProperty(list: java.util.List[String]): Unit = {
    list should have size 3
    list.get(0) shouldBe "zero"
    list.get(1) shouldBe "one"
    list.get(2) shouldBe "two"
  }
  
  "Indexed properties" should "be converted to a list" in {

    val converted: java.util.Map[String, AnyRef] = AkkaConfigPropertySourceAdapter.convertIndexedProperties(goodProperties)
    val list = converted.get("list").asInstanceOf[java.util.List[String]]

    converted.keySet should have size 2
    converted.get("normal") shouldBe "normal"

    validateListProperty(list)
  }

  "Overlapping (bad) property hierarchy" should "throw exception" in {

    an [IllegalArgumentException] should be thrownBy {
      AkkaConfigPropertySourceAdapter.convertIndexedProperties(badProperties)
    }

    // Exception should be thrown regardless of property order
    val reversed = new util.LinkedHashMap[String, String]()
    badProperties.entrySet().asScala.foreach { e => reversed.put(e.getKey, e.getValue) }

    an [IllegalArgumentException] should be thrownBy {
      AkkaConfigPropertySourceAdapter.convertIndexedProperties(reversed)
    }
  }

  "Akka Config" should "parse converted property map" in {
    val converted = AkkaConfigPropertySourceAdapter.convertIndexedProperties(goodProperties)
    val config = ConfigFactory.parseMap(converted)

    config.entrySet should have size 2
    config.hasPath("list") shouldBe true
    config.hasPath("normal") shouldBe true

    validateListProperty(config.getStringList("list"))
  }
}
