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
                    request.getTrainerUsername(),
                    request.getActionType(),
                    transactionId);

            validate(request);

            process(request, transactionId);

        } catch (InvalidWorkloadMessageException e) {
            handleToDlq(request, transactionId, e.getMessage());

        } catch (WorkloadMessageProcessingException e) {
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

            log.info("Workload processed successfully trainer={} txId={}",
                    request.getTrainerUsername(),
                    transactionId);

        } catch (Exception e) {
            throw new WorkloadMessageProcessingException(
                    "Failed to update trainer workload",
                    e
            );
        }
    }

    private void validate(TrainerWorkloadRequest request) {
        if (request == null) {
            throw new InvalidWorkloadMessageException("Request is null");
        }

        if (request.getTrainerUsername() == null || request.getTrainerUsername().isBlank()) {
            throw new InvalidWorkloadMessageException("trainerUsername is required");
        }

        if (request.getTrainingDate() == null) {
            throw new InvalidWorkloadMessageException("trainingDate is required");
        }

        if (request.getActionType() == null) {
            throw new InvalidWorkloadMessageException("actionType is required");
        }
    }

    private void handleToDlq(TrainerWorkloadRequest request,
                             String transactionId,
                             String reason) {

        log.error("Sending message to DLQ. trainer={} reason={} txId={}",
                request.getTrainerUsername(),
                reason,
                transactionId);

        deadLetterPublisher.send(request, reason, transactionId);
    }

    private String extractTransactionId(Message message) {
        try {
            return message.getStringProperty(TRANSACTION_ID_PROPERTY);
        } catch (JMSException e) {
            log.warn("Could not extract transactionId: {}", e.getMessage());
            return null;
        }
    }
}