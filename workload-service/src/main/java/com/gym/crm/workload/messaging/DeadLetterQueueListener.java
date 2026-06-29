package com.gym.crm.workload.messaging;

import gym.crm.platform.workload.openapi.TrainerWorkloadRequest;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeadLetterQueueListener {
    private static final String FAILURE_REASON_PROPERTY = "failureReason";
    private static final String ORIGINAL_DESTINATION_PROPERTY = "originalDestination";
    private static final String TRANSACTION_ID_PROPERTY = "transactionId";

    @JmsListener(destination = "${activemq.destination.trainer-workload-dlq}")
    public void onMessage(TrainerWorkloadRequest request, Message message) {
        String reason = readStringProperty(message, FAILURE_REASON_PROPERTY);
        String originalDestination = readStringProperty(message, ORIGINAL_DESTINATION_PROPERTY);
        String transactionId = readStringProperty(message, TRANSACTION_ID_PROPERTY);

        log.error("Dead letter received from [{}] reason={} trainer={} action={} txId={}",
                originalDestination, reason, request.getTrainerUsername(), request.getActionType(), transactionId);
    }

    private String readStringProperty(Message message, String propertyName) {
        try {
            return message.getStringProperty(propertyName);
        } catch (JMSException exception) {
            log.warn("Could not read property {} from dead letter message: {}", propertyName, exception.getMessage());
            return null;
        }
    }
}