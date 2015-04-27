package com.github.scalaspring.akka.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import org.springframework.core.env.EnumerablePropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Adapts Akka configuration properties to Spring by exposing them as a standard Spring EnumerablePropertySource.
 */
public class AkkaConfigPropertySource extends EnumerablePropertySource<Config> {

    public static final String PROPERTY_SOURCE_NAME = "akkaConfig";

    public AkkaConfigPropertySource(Config config) {
        super(PROPERTY_SOURCE_NAME, config);
    }

    @Override
    public String[] getPropertyNames() {
        final Set<Map.Entry<String, ConfigValue>> entries = source.entrySet();
        final List<String> names = new ArrayList<>(entries.size());

        for (Map.Entry<String, ConfigValue> entry : entries) {
            names.add(entry.getKey());
        }

        return names.toArray(new String[names.size()]);
    }

    @Override
    public Object getProperty(String name) {
        try {
            return source.hasPath(name) ? source.getAnyRef(name) : null;
        } catch (Throwable t) {
            // Catch bad path exceptions and return null
            return null;
        }
    }

}
