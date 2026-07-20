package com.gym.crm.bdd.client;

import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;
import jakarta.jms.Queue;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.util.Map;

@RequiredArgsConstructor
public class JmsQueueClient {
    private final String brokerUrl;
    private final String user;
    private final String password;

    public void sendText(String queueName, String body, Map<String, String> stringProperties) {
        try (JMSContext context = connectionFactory().createContext(user, password)) {
            Queue queue = context.createQueue(queueName);
            TextMessage message = context.createTextMessage(body);

            stringProperties.forEach((key, value) -> {
                try {
                    message.setStringProperty(key, value);
                } catch (JMSException e) {
                    throw new JMSRuntimeException(e.getMessage(), e.getErrorCode(), e);
                }
            });

            context.createProducer().send(queue, message);
        } catch (JMSRuntimeException e) {
            throw new IllegalStateException("Failed to send JMS message to " + queueName, e);
        }
    }

    private ActiveMQConnectionFactory connectionFactory() {
        return new ActiveMQConnectionFactory(brokerUrl);
    }
}