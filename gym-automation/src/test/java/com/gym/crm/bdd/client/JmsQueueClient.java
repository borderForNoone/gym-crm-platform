package com.gym.crm.bdd.client;

import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import jakarta.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.util.Map;

public class JmsQueueClient {
    private final String brokerUrl;
    private final String user;
    private final String password;

    public JmsQueueClient(String brokerUrl, String user, String password) {
        this.brokerUrl = brokerUrl;
        this.user = user;
        this.password = password;
    }

    public void sendText(String queueName, String body, Map<String, String> stringProperties) {
        try (JMSContext context = connectionFactory().createContext(user, password)) {
            Queue queue = context.createQueue(queueName);
            TextMessage message = context.createTextMessage(body);

            for (Map.Entry<String, String> entry : stringProperties.entrySet()) {
                message.setStringProperty(entry.getKey(), entry.getValue());
            }
            context.createProducer().send(queue, message);
        } catch (JMSException e) {
            throw new IllegalStateException("Failed to send JMS message to " + queueName, e);
        } catch (JMSRuntimeException e) {
            throw new IllegalStateException("Failed to connect to broker " + brokerUrl, e);
        }
    }

    public String receiveText(String queueName, long timeoutMillis) {
        try (JMSContext context = connectionFactory().createContext(user, password)) {
            Queue queue = context.createQueue(queueName);
            try (JMSConsumer consumer = context.createConsumer(queue)) {
                Message message = consumer.receive(timeoutMillis);

                return message == null ? null : message.getBody(String.class);
            }
        } catch (JMSException e) {
            throw new IllegalStateException("Failed to read from " + queueName, e);
        } catch (JMSRuntimeException e) {
            throw new IllegalStateException("Failed to connect to broker " + brokerUrl, e);
        }
    }

    private ActiveMQConnectionFactory connectionFactory() {
        return new ActiveMQConnectionFactory(brokerUrl);
    }
}