package com.gym.crm.workload.messaging;

import com.gym.crm.workload.service.TrainerWorkloadService;
import gym.crm.platform.workload.openapi.TrainerWorkloadRequest;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrainerWorkloadMessageListener {
    private static final String TRANSACTION_ID_PROPERTY = "transactionId";
    private static final String MDC_TRANSACTION_ID_KEY = "transactionId";

    private final TrainerWorkloadService trainerWorkloadService;

    @JmsListener(destination = "${activemq.destination.trainer-workload}")
    public void onMessage(TrainerWorkloadRequest request, Message message) {
        String transactionId = extractTransactionId(message);
        MDC.put(MDC_TRANSACTION_ID_KEY, transactionId);

        try {
            log.info("Received workload event trainer={} action={} txId={}",
                    request.getTrainerUsername(), request.getActionType(), transactionId);

            validate(request);
            trainerWorkloadService.updateTrainerWorkload(request);

            log.info("Workload event processed successfully trainer={} txId={}", request.getTrainerUsername(), transactionId);
        } finally {
            MDC.remove(MDC_TRANSACTION_ID_KEY);
        }
    }

    private void validate(TrainerWorkloadRequest request) {
        requireNotNull(request.getTrainerUsername(), "trainerUsername is required");
        requireNotBlank(request.getTrainerUsername(), "trainerUsername is required");
        requireNotNull(request.getTrainingDate(), "trainingDate is required");
        requireNotNull(request.getActionType(), "actionType is required");
    }

    private void requireNotNull(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }

    private void requireNotBlank(String value, String message) {
        if (value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private String extractTransactionId(Message message) {
        try {
            return message.getStringProperty(TRANSACTION_ID_PROPERTY);
        } catch (JMSException exception) {
            log.warn("Could not extract transactionId from message: {}", exception.getMessage());

            return null;
        }
    }
}