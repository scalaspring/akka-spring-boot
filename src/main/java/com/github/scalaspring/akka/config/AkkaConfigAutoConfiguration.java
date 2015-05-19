package com.github.scalaspring.akka.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Map;

import static com.github.scalaspring.akka.config.AkkaConfigPropertySourceAdapter.convertIndexedProperties;
import static com.github.scalaspring.akka.config.AkkaConfigPropertySourceAdapter.getPropertyMap;

/**
 * Links Spring environment (property sources) and Akka configuration (Config).
 * This allows Akka configuration to be specified via standard Spring-based property source(s) and also allows
 * Spring to access Akka-based defaults.
 *
 * This configuration provides the following:
 *
 * 1. An Akka configuration bean populated with Akka defaults, overridden by Spring defined properties. This allows
 *    Akka configuration to be specified via any Spring supported property source (files, URLs, YAML, etc.) while
 *    still leveraging Akka defaults when not specified.
 * 2. An optional property sources placeholder configurer. Created if not defined elsewhere, this bean processes @Value
 *    annotations that reference properties.
 *
 * Note: This class is intentionally written in Java to enable use in Java-only projects and due to the fact that
 * a static factory method is required for the {@code PropertySourcesPlaceholderConfigurer} bean. Do not instantiate
 * this bean in a non-static method, especially in configurations that have autowired properties. You've been warned.
 */
@Configuration
@ComponentScan
public class AkkaConfigAutoConfiguration {

    /**
     * Creates an Akka configuration populated via Spring properties with fallback to Akka defined properties.
     */
    @Bean
    public Config akkaConfig(ConfigurableApplicationContext applicationContext, ConfigurableEnvironment environment) {
        final Map<String, Object> converted = AkkaConfigPropertySourceAdapter.convertIndexedProperties(AkkaConfigPropertySourceAdapter.getPropertyMap(environment));
        final Config defaultConfig = ConfigFactory.defaultReference(applicationContext.getClassLoader());

        return ConfigFactory.parseMap(converted).withFallback(defaultConfig);
    }

    /**
     * Create placeholder configurer to support {@code @Value} annotations that reference environment properties.
     *
     * Only one placeholder configurer is required, hence the conditional
     */
    @Bean @ConditionalOnMissingBean(PropertySourcesPlaceholderConfigurer.class)
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

}
