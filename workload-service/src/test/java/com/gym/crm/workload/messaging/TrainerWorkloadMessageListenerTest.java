package com.gym.crm.workload.messaging;

import com.gym.crm.workload.service.TrainerWorkloadService;
import gym.crm.platform.workload.openapi.ActionType;
import gym.crm.platform.workload.openapi.TrainerWorkloadRequest;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadMessageListenerTest {
    private static final String TRANSACTION_ID_PROPERTY = "transactionId";
    private static final String USERNAME = "billy.herrington";

    @Mock
    private TrainerWorkloadService trainerWorkloadService;
    @Mock
    private DeadLetterPublisher deadLetterPublisher;
    @Mock
    private Message message;

    private TrainerWorkloadMessageListener listener;

    @BeforeEach
    void setUp() {
        listener = new TrainerWorkloadMessageListener(trainerWorkloadService, deadLetterPublisher);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void onMessage_shouldCallService_whenRequestIsValid() throws JMSException {
        TrainerWorkloadRequest request = buildRequest();
        when(message.getStringProperty("transactionId")).thenReturn("tx-123");

        listener.onMessage(request, message);

        verify(trainerWorkloadService).updateTrainerWorkload(request);
        verify(deadLetterPublisher, never()).send(any(), anyString(), anyString());
    }

    @Test
    void onMessage_shouldRouteToDeadLetter_whenTrainerUsernameIsNull() throws JMSException {
        TrainerWorkloadRequest request = buildRequest().trainerUsername(null);
        when(message.getStringProperty("transactionId")).thenReturn("tx-123");

        listener.onMessage(request, message);

        verify(deadLetterPublisher).send(eq(request), eq("tx-123"), anyString());
        verify(trainerWorkloadService, never()).updateTrainerWorkload(any());
    }

    @Test
    void onMessage_shouldRouteToDeadLetter_whenTrainerUsernameIsBlank() throws JMSException {
        TrainerWorkloadRequest request = buildRequest().trainerUsername("   ");
        when(message.getStringProperty("transactionId")).thenReturn("tx-123");

        listener.onMessage(request, message);

        verify(deadLetterPublisher).send(eq(request), eq("tx-123"), anyString());
    }

    @Test
    void onMessage_shouldRouteToDeadLetter_whenTrainingDateIsNull() throws JMSException {
        TrainerWorkloadRequest request = buildRequest().trainingDate(null);
        when(message.getStringProperty("transactionId")).thenReturn("tx-123");

        listener.onMessage(request, message);

        verify(deadLetterPublisher).send(eq(request), eq("tx-123"), anyString());
    }

    @Test
    void onMessage_shouldProcessMessage_whenActionTypeIsNull() throws JMSException {
        TrainerWorkloadRequest request = buildRequest().actionType(null);
        when(message.getStringProperty("transactionId")).thenReturn("tx-123");

        listener.onMessage(request, message);

        verify(trainerWorkloadService).updateTrainerWorkload(request);
        verify(deadLetterPublisher, never()).send(any(), anyString(), anyString());
    }

    @Test
    void onMessage_shouldNotThrow_whenRoutingToDeadLetter() throws JMSException {
        TrainerWorkloadRequest request = buildRequest().trainerUsername(null);

        when(message.getStringProperty(TRANSACTION_ID_PROPERTY)).thenReturn("tx-123");

        assertThatNoException().isThrownBy(() -> listener.onMessage(request, message));
    }

    @Test
    void onMessage_shouldSendToDlq_whenServiceCallFails() throws JMSException {
        TrainerWorkloadRequest request = buildRequest();

        when(message.getStringProperty(TRANSACTION_ID_PROPERTY)).thenReturn("tx-123");
        doThrow(new RuntimeException("database unavailable")).when(trainerWorkloadService).updateTrainerWorkload(request);

        listener.onMessage(request, message);

        verify(deadLetterPublisher).send(eq(request), eq("tx-123"), eq("Failed to update trainer workload"));
        verify(trainerWorkloadService).updateTrainerWorkload(request);
    }

    @Test
    void onMessage_shouldSendToDlq_whenServiceFails() throws JMSException {
        TrainerWorkloadRequest request = buildRequest();

        when(message.getStringProperty("transactionId")).thenReturn("tx-123");
        doThrow(new RuntimeException("database unavailable")).when(trainerWorkloadService).updateTrainerWorkload(request);

        listener.onMessage(request, message);

        verify(deadLetterPublisher).send(eq(request), eq("tx-123"), eq("Failed to update trainer workload"));
        verify(trainerWorkloadService).updateTrainerWorkload(request);
    }

    @Test
    void onMessage_shouldSendToDlq_whenUnexpectedErrorOccurs() throws JMSException {
        TrainerWorkloadRequest request = buildRequest();

        when(message.getStringProperty(TRANSACTION_ID_PROPERTY)).thenReturn("tx-999");
        doThrow(new RuntimeException("unexpected crash")).when(trainerWorkloadService).updateTrainerWorkload(request);

        listener.onMessage(request, message);

        verify(deadLetterPublisher).send(eq(request), eq("tx-999"), anyString());
    }

    @Test
    void onMessage_shouldRouteToDlq_whenRequestIsNull() throws JMSException {
        when(message.getStringProperty(TRANSACTION_ID_PROPERTY)).thenReturn("tx-null");

        listener.onMessage(null, message);

        verify(deadLetterPublisher).send(eq(null), eq("tx-null"), eq("Request is null"));
        verify(trainerWorkloadService, never()).updateTrainerWorkload(any());
    }

    @Test
    void onMessage_shouldUseNoTxn_whenJmsExceptionThrown() throws JMSException {
        TrainerWorkloadRequest request = buildRequest();

        when(message.getStringProperty(TRANSACTION_ID_PROPERTY)).thenThrow(new JMSException("boom"));

        listener.onMessage(request, message);

        verify(trainerWorkloadService).updateTrainerWorkload(request);
        verify(deadLetterPublisher, never()).send(any(), anyString(), anyString());
    }

    private TrainerWorkloadRequest buildRequest() {
        return new TrainerWorkloadRequest()
                .trainerUsername(USERNAME)
                .trainerFirstName("Billy")
                .trainerLastName("Herrington")
                .isActive(true)
                .trainingDate(LocalDate.of(2026, Month.JUNE, 10))
                .trainingDuration(60)
                .actionType(ActionType.ADD);
    }
}