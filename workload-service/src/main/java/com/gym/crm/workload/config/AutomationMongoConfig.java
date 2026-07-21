package com.gym.crm.workload.config;

import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.transitions.Start;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("automation")
public class AutomationMongoConfig {
    public static final int EMBEDDED_MONGO_PORT = 27117;

    @Bean(destroyMethod = "close")
    public TransitionWalker.ReachedState<RunningMongodProcess> embeddedMongo() {
        Mongod mongod = Mongod.builder()
                .net(Start.to(Net.class).initializedWith(Net.defaults().withPort(EMBEDDED_MONGO_PORT)))
                .build();
        return mongod.start(Version.Main.V7_0);
    }
}