package com.gym.crm.bdd.config;

public final class TestProperties {
    private static final String DEFAULT_CORE_URL = "http://localhost:8080/gym-crm-application/api/v1";
    private static final String DEFAULT_WORKLOAD_URL = "http://localhost:8080/workload-service/api/v1";

    private TestProperties() {
    }

    public static String coreBaseUrl() {
        return property("system.tests.core.base-url", DEFAULT_CORE_URL);
    }

    public static String workloadBaseUrl() {
        return property("system.tests.workload.base-url", DEFAULT_WORKLOAD_URL);
    }

    public static String defaultUsername() {
        return property("system.tests.user.username", "billy.herrington");
    }

    public static String defaultPassword() {
        return property("system.tests.user.password", "password");
    }

    private static String property(String name, String defaultValue) {
        return System.getProperty(name, defaultValue);
    }
}