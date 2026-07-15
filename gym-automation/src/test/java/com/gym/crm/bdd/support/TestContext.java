package com.gym.crm.bdd.support;

import io.restassured.response.Response;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class TestContext {
    private final Map<String, Object> values = new HashMap<>();

    @Setter
    @Getter
    private Response lastResponse;
    @Setter
    @Getter
    private String token;

    public void put(String key, Object value) {
        values.put(key, value);
    }

    public String getString(String key) {
        Object value = values.get(key);
        return value == null ? null : value.toString();
    }
}