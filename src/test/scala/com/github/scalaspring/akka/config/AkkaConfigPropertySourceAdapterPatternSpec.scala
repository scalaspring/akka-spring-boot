package com.github.scalaspring.akka.config

import java.util.regex.Matcher

import com.typesafe.scalalogging.StrictLogging
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.{FlatSpec, Matchers}

class AkkaConfigPropertySourceAdapterPatternSpec extends FlatSpec with Matchers with StrictLogging {

  val indexed = Table(
    ("name",                      "path",                 "index"),
    ("x[0]",                      "x",                    0),
    ("someProperty[0]",           "someProperty",         0),
    ("some_property[1]",          "some_property",        1),
    ("some.property[0]",          "some.property",        0),
    (" some.property[0] ",        "some.property",        0),
    ("some.other.property[893]",  "some.other.property",  893)
  )

  val nonIndexed = Table(
    ("name"),
    ("x"),
    ("someProperty"),
    ("some_property"),
    ("some.property"),
    ("some.other.property")
  )

  "Indexed property regular expression" should "match indexed property names" in {
    forAll (indexed) { (name: String, path: String, index: Int) =>
      val m: Matcher = AkkaConfigPropertySourceAdapter.INDEXED_PROPERTY_PATTERN.matcher(name)
      m.matches() shouldBe true
      m.group("path") shouldEqual path
      m.group("index") shouldEqual index.toString
    }
  }

  it should "not match non-indexed property names" in {
    forAll (nonIndexed) { (name: String) =>
      val m: Matcher = AkkaConfigPropertySourceAdapter.INDEXED_PROPERTY_PATTERN.matcher(name)
      m.matches() shouldBe false
    }
  }

}
