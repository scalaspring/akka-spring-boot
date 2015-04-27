package com.github.scalaspring.akka

import org.apache.commons.logging.{Log, LogFactory}

trait SpringLogging {
  protected val log: Log = LogFactory.getLog(getClass.getName)
}
