package com.gym.crm.bdd.config;

import lombok.experimental.UtilityClass;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

@UtilityClass
public class TestProperties {
    private static final Map<String, Object> CONFIG = load();

    public static String coreBaseUrl() {
        return value("system", "tests", "core", "base-url");
    }

    public static String workloadBaseUrl() {
        return value("system", "tests", "workload", "base-url");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> load() {
        try (InputStream input = TestProperties.class.getClassLoader().getResourceAsStream("application.yml")) {
            if (input == null) {
                throw new IllegalStateException("application.yml not found");
            }

            return new Yaml().load(input);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load application.yml", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static String value(String... path) {
        Object current = CONFIG;

        for (String key : path) {
            current = ((Map<String, Object>) current).get(key);
        }

        return current.toString();
    }
}