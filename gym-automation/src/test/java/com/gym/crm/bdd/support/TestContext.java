package com.gym.crm.bdd.support;

import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

public class TestContext {
    private Response lastResponse;
    private String token;
    private final Map<String, Object> values = new HashMap<>();

    public Response getLastResponse() {
        return lastResponse;
    }

    public void setLastResponse(Response lastResponse) {
        this.lastResponse = lastResponse;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void put(String key, Object value) {
        values.put(key, value);
    }

    public String getString(String key) {
        Object value = values.get(key);
        return value == null ? null : value.toString();
    }
}