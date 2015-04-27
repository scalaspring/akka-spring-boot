package com.github.scalaspring.akka.config;

import com.typesafe.config.ConfigFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Adds Akka configuration as a property source in the current application context.
 * This class is NOT for direct use by developers.
 *
 * Usage: Import the AkkaAutoConfiguration configuration to create an instance of this class via component scanning.
 *
 * @see com.github.scalaspring.akka.AkkaAutoConfiguration
 */
@Component
public class AkkaConfigPropertySourceBeanFactoryPostProcessor implements BeanFactoryPostProcessor, ApplicationContextAware, EnvironmentAware {

    private static Log log = LogFactory.getLog(AkkaConfigPropertySourceBeanFactoryPostProcessor.class);

    protected ApplicationContext applicationContext = null;
    protected ConfigurableEnvironment environment = null;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        log.debug("Adding Akka Config property source to bean factory");
        environment.getPropertySources().addLast(new AkkaConfigPropertySource(ConfigFactory.load(applicationContext.getClassLoader())));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }

}
