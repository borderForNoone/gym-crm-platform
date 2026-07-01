package com.gym.crm.workload.config;

import jakarta.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.util.ErrorHandler;

import java.util.List;

@Configuration
public class JmsConfig {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JmsConfig.class);

    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;
    @Value("${spring.activemq.user}")
    private String user;
    @Value("${spring.activemq.password}")
    private String password;
    @Value("${activemq.redelivery.max-redeliveries:3}")
    private int maxRedeliveries;

    @Bean
    public ErrorHandler jmsErrorHandler() {
        return trThrowable -> log.error("Unhandled JMS error", trThrowable);
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        factory.setUserName(user);
        factory.setPassword(password);
        factory.setTrustedPackages(List.of("gym.crm.platform.workload.openapi"));
        factory.setTrustAllPackages(false);

        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
        redeliveryPolicy.setMaximumRedeliveries(maxRedeliveries);
        redeliveryPolicy.setInitialRedeliveryDelay(1000);
        redeliveryPolicy.setUseExponentialBackOff(true);
        redeliveryPolicy.setBackOffMultiplier(2.0);

        factory.setRedeliveryPolicy(redeliveryPolicy);

        return factory;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory connectionFactory, DefaultJmsListenerContainerFactoryConfigurer configurer,
            MessageConverter messageConverter,
            ErrorHandler jmsErrorHandler,
            @Value("${workload.messaging.consumer.concurrency}") String concurrency) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();

        configurer.configure(factory, connectionFactory);

        factory.setMessageConverter(messageConverter);
        factory.setSessionTransacted(true);
        factory.setErrorHandler(jmsErrorHandler);
        factory.setConcurrency(concurrency);

        return factory;
    }

    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        JmsTemplate template = new JmsTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);

        return template;
    }

    @Bean
    public MessageConverter messageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        converter.setTypeIdMappings(java.util.Map.of(
                "TrainerWorkloadRequest", gym.crm.platform.workload.openapi.TrainerWorkloadRequest.class
        ));

        return converter;
    }
}