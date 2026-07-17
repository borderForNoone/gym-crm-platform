package com.gym.crm.bdd.config;

import io.cucumber.java.Before;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MongoCleanupHook {

    private final MongoTemplate mongoTemplate;

    @Before("@integration")
    public void cleanMongo() {
        long before = mongoTemplate.getCollection("trainer_workloads").countDocuments();

        mongoTemplate.getCollection("trainer_workloads").deleteMany(new org.bson.Document());

        long after = mongoTemplate
                .getCollection("trainer_workloads")
                .countDocuments();
    }
}
