package com.gym.crm.bdd.config;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.MySQLContainer;

public class TestEnvironment {
    public static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0").withDatabaseName("gym").withUsername("gym").withPassword("gym");
    public static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7");
    public static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7").withExposedPorts(6379);

    static {
        MYSQL.start();
        MONGO.start();
        REDIS.start();
    }
}