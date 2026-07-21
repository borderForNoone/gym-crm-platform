package com.gym.crm.bdd.support;

import io.restassured.response.Response;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class TestContext {
    private Response lastResponse;
    private String token;
    private final Map<String, Object> values = new HashMap<>();

    public void put(String key, Object value) {
        values.put(key, value);
    }

    public String getString(String key) {
        Object value = values.get(key);

        return value == null ? null : value.toString();
    }
}