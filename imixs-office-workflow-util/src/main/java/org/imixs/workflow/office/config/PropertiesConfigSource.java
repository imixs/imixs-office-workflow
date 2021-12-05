package org.imixs.workflow.office.config;

import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.config.spi.ConfigSource;

/**
 * The Marty PropertiesConfigSource is a custom config source based on
 * Microprofile Config API.
 * <p>
 * The properties of this config source are loaded by the Marty PropertiesLoader
 * EJB.
 * <p>
 * As per SPI it is necessary to register the implementation in
 * META-INF/services by adding an entry in a file called
 * 'org.eclipse.microprofile.config.spi.ConfigSource'
 * <p>
 * See implementation details here:
 * https://stackoverflow.com/questions/52117613/microprofile-config-custom-configsource-using-jpa/56498321#56498321
 * 
 * @see PropertiesLoader
 * @author rsoika
 *
 */
public class PropertiesConfigSource implements ConfigSource {

    public static final String NAME = "MartyConfigSource";
    public static Map<String, String> properties = null; // use static to overwrite early registered properties

    @Override
    public int getOrdinal() {
        return 910;
    }

    @Override
    public String getValue(String key) {
        if (properties != null) {
            return properties.get(key);
        } else {
            return null;
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public Set<String> getPropertyNames() {
        return properties.keySet();
    }

}