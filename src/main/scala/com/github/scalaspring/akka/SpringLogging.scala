package com.github.scalaspring.akka

import org.apache.commons.logging.{Log, LogFactory}
import org.springframework.util.ClassUtils

trait SpringLogging {
  // Remove any CGLIB gunk to clean up logging
  protected val log: Log = LogFactory.getLog(ClassUtils.getUserClass(getClass))
}
