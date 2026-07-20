package com.gym.crm.bdd.config;

import lombok.experimental.UtilityClass;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class TestProperties {
    private static final String PROPERTIES_FILE = "application.yml";
    private static final Map<String, String> DEFAULTS = load();

    public String coreBaseUrl() {
        return property("system.tests.core.base-url");
    }

    public String workloadBaseUrl() {
        return property("system.tests.workload.base-url");
    }

    public String jmsBrokerUrl() {
        return property("system.tests.jms.broker-url");
    }

    public String jmsUser() {
        return property("system.tests.jms.user");
    }

    public String jmsPassword() {
        return property("system.tests.jms.password");
    }

    private String property(String name) {
        String override = System.getProperty(name);
        if (override != null && !override.isBlank()) {
            return override;
        }

        String fromFile = DEFAULTS.get(name);
        if (fromFile == null) {
            throw new IllegalStateException("Missing property '" + name + "': set it in " + PROPERTIES_FILE + " or pass -D" + name + "=... on the command line");
        }

        return fromFile;
    }

    private Map<String, String> load() {
        Yaml yaml = new Yaml();

        try (InputStream inputStream = TestProperties.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (inputStream == null) {
                throw new IllegalStateException(PROPERTIES_FILE + " not found on the test classpath");
            }

            Object loaded = yaml.load(inputStream);
            if (!(loaded instanceof Map<?, ?> raw)) {
                throw new IllegalStateException("Top-level YAML must be a map, got: " + loaded);
            }

            Map<String, String> flat = new HashMap<>();
            flatten("", raw, flat);

            return flat;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load " + PROPERTIES_FILE, e);
        }
    }

    private void flatten(String prefix, Map<?, ?> node, Map<String, String> out) {
        for (Map.Entry<?, ?> entry : node.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey().toString() : prefix + "." + entry.getKey().toString();
            Object value = entry.getValue();

            if (value instanceof Map<?, ?> nested) {
                flatten(key, nested, out);
            } else {
                out.put(key, String.valueOf(value));
            }
        }
    }
}