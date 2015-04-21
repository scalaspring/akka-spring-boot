package com.github.lancearlaus.akka.spring

import org.apache.commons.logging.{Log, LogFactory}

trait SpringLogging {
  protected val log: Log = LogFactory.getLog(getClass.getName)
}
