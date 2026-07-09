package com.gym.crm.workload.config;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;

public abstract class AbstractMongoRepositoryTest<T extends MongoRepository<?, ?>> {
    private static final String MONGO_IMAGE = "mongo:7.0.12";
    private static final MongoDBContainer MONGO = new MongoDBContainer(MONGO_IMAGE);

    static {
        MONGO.start();
    }

    @Autowired
    protected T repository;

    @DynamicPropertySource
    static void setMongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
    }

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }
}
