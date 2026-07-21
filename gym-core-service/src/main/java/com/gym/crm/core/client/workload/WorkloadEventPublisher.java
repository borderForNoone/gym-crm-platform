package com.gym.crm.core.client.workload;

import com.gym.crm.core.client.workload.model.TrainerWorkloadRequest;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkloadEventPublisher {
    private static final String TRANSACTION_ID_PROPERTY = "transactionId";
    private static final String MDC_TRANSACTION_ID_KEY = "transactionId";

    private final JmsTemplate jmsTemplate;

    @Value("${activemq.destination.trainer-workload}")
    private String destination;

    public void publish(TrainerWorkloadRequest request) {
        String transactionId = MDC.get(MDC_TRANSACTION_ID_KEY);
        log.info("Publishing workload event [{}] action={} trainer={} txId={}",
                destination, request.getActionType(), request.getTrainerUsername(), transactionId);

        try {
            jmsTemplate.convertAndSend(destination, request, message -> withTransactionId(message, transactionId));
            log.info("Workload event published for trainer={} action={} txId={}", request.getTrainerUsername(), request.getActionType(), transactionId);
        } catch (JmsException exception) {
            log.error("Failed to publish workload event for trainer={} action={} txId={}", request.getTrainerUsername(), request.getActionType(), transactionId, exception);
            throw exception;
        }
    }

    private Message withTransactionId(Message message, String transactionId) throws JMSException {
        if (transactionId != null) {
            message.setStringProperty(TRANSACTION_ID_PROPERTY, transactionId);
        }

        return message;
    }
}