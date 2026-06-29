package com.gym.crm.workload.messaging;

import gym.crm.platform.workload.openapi.ActionType;
import gym.crm.platform.workload.openapi.TrainerWorkloadRequest;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeadLetterQueueListenerTest {
    private static final String USERNAME = "billy.herrington";

    @Mock
    private Message message;

    private final DeadLetterQueueListener listener = new DeadLetterQueueListener();

    @Test
    void onMessage_shouldNotThrow_whenAllPropertiesArePresent() throws JMSException {
        TrainerWorkloadRequest request = buildRequest();

        when(message.getStringProperty("failureReason")).thenReturn("trainerUsername is required");
        when(message.getStringProperty("originalDestination")).thenReturn("trainer-workload-queue");
        when(message.getStringProperty("transactionId")).thenReturn("tx-123");

        assertThatNoException().isThrownBy(() -> listener.onMessage(request, message));
    }

    @Test
    void onMessage_shouldNotThrow_whenPropertyExtractionFails() throws JMSException {
        TrainerWorkloadRequest request = buildRequest();

        when(message.getStringProperty("failureReason")).thenThrow(new JMSException("broker error"));

        assertThatNoException().isThrownBy(() -> listener.onMessage(request, message));
    }

    private TrainerWorkloadRequest buildRequest() {
        return new TrainerWorkloadRequest()
                .trainerUsername(USERNAME)
                .actionType(ActionType.ADD);
    }
}