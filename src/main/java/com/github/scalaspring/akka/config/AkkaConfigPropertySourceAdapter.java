package com.github.scalaspring.akka.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods for adapting Spring properties to Akka Config.
 */
public class AkkaConfigPropertySourceAdapter {

    private static Log log = LogFactory.getLog(AkkaConfigPropertySourceAdapter.class);

    public static final Pattern INDEXED_PROPERTY_PATTERN = Pattern.compile("^\\s*(?<path>\\w+(?:\\.\\w+)*)\\[(?<index>\\d+)\\]\\s*$");

    /**
     * Convert list properties specified in properties format into lists, replacing the original properties.
     *
     * Before:
     *   list.property[0]=zero
     *   list.property[1]=one
     *   list.property[2]=two
     * After
     *   list.property={@code List<String>("zero", "one", "two")}
     *
     * @param propertyMap property map possibly containing indexed properties
     * @return property map (a copy) with indexed properties converted to lists
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> convertIndexedProperties(Map<String, String> propertyMap) {

        if (CollectionUtils.isEmpty(propertyMap)) {
            return Collections.emptyMap();
        }

        final Map<String, Object> converted = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry: propertyMap.entrySet()) {
            final Matcher m = INDEXED_PROPERTY_PATTERN.matcher(entry.getKey());
            final String path = m.matches() ? m.group("path") : entry.getKey();
            final Object existing = converted.get(path);

            // Make sure properties are either list-based or normal
            if ((existing != null) && ((m.matches() && !(existing instanceof List)) || (!m.matches() && !(existing instanceof String)))) {
                throw new IllegalArgumentException("Invalid property hierarchy - property must be either a regular or a list-based property (path:" + path + ", existing: " + existing + ", key:" + entry.getKey() + ", value:" + entry.getValue());
            }

            // Convert indexed properties to lists
            if (m.matches()) {

                final int index = Integer.parseInt(m.group("index"));
                final ArrayList<String> list = converted.containsKey(path) ? (ArrayList<String>) converted.get(path) : new ArrayList<String>();

                // Extend the list, if necessary
                list.ensureCapacity(index + 1);
                while (list.size() <= index) {
                    list.add(null);
                }

                list.set(index, entry.getValue());
                converted.put(path, list);

            } else {
                // Copy normal properties
                converted.put(entry.getKey(), entry.getValue());
            }
        }

        // Make sure any contained lists are immutable
        for (Map.Entry<String, Object> entry : converted.entrySet()) {
            if (entry.getValue() instanceof List) {
                entry.setValue(Collections.unmodifiableList((List<String>) entry.getValue()));
            }
        }

        return Collections.unmodifiableMap(converted);
    }

    public static Map<String, String> getPropertyMap(ConfigurableEnvironment environment) {
        final Map<String, String> propertyMap = new HashMap<>();

        for (final PropertySource source : environment.getPropertySources()) {
            if (isEligiblePropertySource(source)) {
                final EnumerablePropertySource enumerable = (EnumerablePropertySource) source;

                log.debug("Adding properties from property source " + source.getName());

                for (final String name : enumerable.getPropertyNames()) {
                    if (isEligibleProperty(name) && !propertyMap.containsKey(name)) {
                        if (log.isTraceEnabled()) log.trace("Adding property " + name);
                        propertyMap.put(name, environment.getProperty(name));
                    }
                }
            }
        }

        return Collections.unmodifiableMap(propertyMap);
    }

    public static boolean isEligiblePropertySource(PropertySource source) {
        // Eliminate system environment properties and system properties sources
        // since these sources are already included in the default configuration
        final String name = source.getName();
        return (source instanceof EnumerablePropertySource) &&
                !(
                    name.equalsIgnoreCase(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME) ||
                    name.equalsIgnoreCase(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME) ||
                    name.equalsIgnoreCase(AkkaConfigPropertySource.PROPERTY_SOURCE_NAME)
                );
    }

    public static boolean isEligibleProperty(String name) {
        return !name.startsWith("spring.");
    }
}
