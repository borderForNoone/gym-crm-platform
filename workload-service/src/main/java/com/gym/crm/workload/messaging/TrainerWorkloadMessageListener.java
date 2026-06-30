package com.gym.crm.workload.messaging;

import com.gym.crm.workload.exception.InvalidWorkloadMessageException;
import com.gym.crm.workload.exception.WorkloadMessageProcessingException;
import com.gym.crm.workload.service.TrainerWorkloadService;
import gym.crm.platform.workload.openapi.TrainerWorkloadRequest;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrainerWorkloadMessageListener {
    private static final String TRANSACTION_ID_PROPERTY = "transactionId";
    private static final String MDC_TRANSACTION_ID_KEY = "transactionId";

    private final TrainerWorkloadService trainerWorkloadService;
    private final DeadLetterPublisher deadLetterPublisher;

    @JmsListener(destination = "${activemq.destination.trainer-workload}")
    public void onMessage(TrainerWorkloadRequest request, Message message) {
        String transactionId = extractTransactionId(message);
        MDC.put(MDC_TRANSACTION_ID_KEY, transactionId);

        try {
            log.info("Received workload event trainer={} action={} txId={}",
                    request != null ? request.getTrainerUsername() : null,
                    request != null ? request.getActionType() : null,
                    transactionId
            );

            validate(request);
            process(request, transactionId);

        } catch (InvalidWorkloadMessageException | WorkloadMessageProcessingException e) {
            handleToDlq(request, transactionId, e.getMessage());

        } catch (Exception e) {
            log.error("Unexpected error while processing workload message", e);
            handleToDlq(request, transactionId, "Unexpected error: " + e.getMessage());
        } finally {
            MDC.remove(MDC_TRANSACTION_ID_KEY);
        }
    }

    private void process(TrainerWorkloadRequest request, String transactionId) {
        try {
            trainerWorkloadService.updateTrainerWorkload(request);

            log.info("Workload processed successfully trainer={} txId={}", request.getTrainerUsername(), transactionId);
        } catch (Exception e) {
            throw new WorkloadMessageProcessingException("Failed to update trainer workload", e);
        }
    }

    private void validate(TrainerWorkloadRequest request) {
        if (request == null) {
            throw new InvalidWorkloadMessageException("Request is null");
        }

        if (request.getTrainerUsername() == null || request.getTrainerUsername().isBlank()) {
            throw new InvalidWorkloadMessageException("trainerUsername is required");
        }

        LocalDate trainingDate = request.getTrainingDate();

        if (trainingDate.isAfter(LocalDate.now())) {
            throw new InvalidWorkloadMessageException("trainingDate cannot be in the future");
        }
    }

    private String extractTransactionId(Message message) {
        try {
            return message.getStringProperty(TRANSACTION_ID_PROPERTY);
        } catch (JMSException e) {
            log.warn("Cannot extract transactionId from JMS message");
            return "no-txn";
        }
    }

    private void handleToDlq(TrainerWorkloadRequest request, String transactionId, String reason) {
        log.warn("Sending message to DLQ txId={} reason={}", transactionId, reason);
        deadLetterPublisher.send(request, transactionId, reason);
    }
}