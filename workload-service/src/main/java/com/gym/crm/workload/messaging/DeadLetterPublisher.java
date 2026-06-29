package com.gym.crm.workload.messaging;

import gym.crm.platform.workload.openapi.TrainerWorkloadRequest;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeadLetterPublisher {
    private static final String FAILURE_REASON_PROPERTY = "failureReason";
    private static final String ORIGINAL_DESTINATION_PROPERTY = "originalDestination";
    private static final String TRANSACTION_ID_PROPERTY = "transactionId";

    private final JmsTemplate jmsTemplate;

    @Value("${activemq.destination.trainer-workload-dlq}")
    private String dlqDestination;

    @Value("${activemq.destination.trainer-workload}")
    private String sourceDestination;

    public void send(TrainerWorkloadRequest request, String reason, String transactionId) {
        log.warn("Routing invalid workload message to DLQ [{}] reason={} trainer={} txId={}",
                dlqDestination, reason, request.getTrainerUsername(), transactionId);

        jmsTemplate.convertAndSend(dlqDestination, request, message -> enrich(message, reason, transactionId));
    }

    private Message enrich(Message message, String reason, String transactionId) throws JMSException {
        message.setStringProperty(FAILURE_REASON_PROPERTY, reason);
        message.setStringProperty(ORIGINAL_DESTINATION_PROPERTY, sourceDestination);
        if (transactionId != null) {
            message.setStringProperty(TRANSACTION_ID_PROPERTY, transactionId);
        }

        return message;
    }
}