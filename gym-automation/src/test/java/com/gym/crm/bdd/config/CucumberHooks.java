package com.gym.crm.bdd.config;

import io.cucumber.java.AfterAll;

public class CucumberHooks {
    @AfterAll
    public static void cleanup() {
        TestEnvironment.MYSQL.stop();
        TestEnvironment.MONGO.stop();
        TestEnvironment.REDIS.stop();
    }
}